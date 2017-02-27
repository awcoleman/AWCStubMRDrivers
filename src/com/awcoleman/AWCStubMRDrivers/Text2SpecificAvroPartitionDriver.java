package com.awcoleman.AWCStubMRDrivers;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileConstants;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyOutputFormat;
import org.apache.avro.mapreduce.AvroMultipleOutputs;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.awcoleman.examples.avro.EventThreeFieldRecord;

/*
 * Example Driver to take text file of format:
 * UUID    DateTime              Category
 * Long    String                Int
 * 11937   2015-02-22 23:59:59   3
 * 
 * and write to Avro files in directories suited for Hive partitions of Day and Hour
 * 
 * Map-only MR job since no reason to have reducer.
 * 
 * To compile Avro Schema to Java Class:
 * avro-tools compile schema com.awcoleman.examples.EventThreeFieldRecord.avsc .
 * 
 * #Prep dirs
 * hdfs dfs -mkdir -p /tmp/Text2SpecificAvroPartitionDriver/input
 * hdfs dfs -put -p /home/cloudera/temp/testFile.EventThreeFieldRecord.txt /tmp/Text2SpecificAvroPartitionDriver/input
 * 
 * #Normal usage
 * export USERCLASSPATH=/usr/lib/avro/avro-mapred-hadoop2.jar:/JavaLibraries/commons-lang3-3.5.jar
 * export CLASSPATH=$(hadoop classpath):${USERCLASSPATH}
 * export HADOOP_CLASSPATH=${CLASSPATH}
 * export LIBJARS=$(echo ${USERCLASSPATH} | sed s/:/,/g)
 * export LD_LIBRARY_PATH=/usr/lib/hadoop/lib/native:$LD_LIBRARY_PATH
 * 
 * hadoop jar /tmp/Drivers.jar com.awcoleman.AWCStubMRDrivers.Text2SpecificAvroPartitionDriver -libjars ${LIBJARS} /tmp/Text2SpecificAvroPartitionDriver/input /tmp/Text2SpecificAvroPartitionDriver/output
 * 
 * #Check output
 * hdfs dfs -ls -R /tmp/Text2SpecificAvroPartitionDriver/output
 * 
 * avro-tools tojson hdfs://localhost:8020/tmp/Text2SpecificAvroPartitionDriver/output/pdate=2015-02-21/phour=21/eventthreefieldrecord-m-00000.avro
 * 
 * #Create external table (in prod use better schema mgmt than this) and repair MSCK to pick up partition directory structure
 * hdfs dfs -put -f ~/temp/com.awcoleman.examples.EventThreeFieldRecord.avsc /tmp
 * 
 * beeline -u jdbc:hive2://localhost:10000/default -n username -p password
 * CREATE EXTERNAL TABLE Text2SpecificAvroPartitionTest
 *   PARTITIONED BY (pdate STRING, phour STRING)
 *   ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.avro.AvroSerDe'
 *   STORED AS INPUTFORMAT 'org.apache.hadoop.hive.ql.io.avro.AvroContainerInputFormat'
 *   OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.avro.AvroContainerOutputFormat'
 *   LOCATION '/tmp/Text2SpecificAvroPartitionDriver/output'
 *   TBLPROPERTIES ('avro.schema.url'='hdfs://localhost:8020/tmp/com.awcoleman.examples.EventThreeFieldRecord.avsc')
 *   ;
 * MSCK REPAIR TABLE Text2SpecificAvroPartitionTest;
 * 
 * beeline -u jdbc:hive2://localhost:10000/default -n username -p password -e "SELECT * FROM Text2SpecificAvroPartitionTest;"
 * 
 * #Clean
 * hdfs dfs -rm -r /tmp/Text2SpecificAvroPartitionDriver/output
 * 
 * @author awcoleman
 * license: Apache License 2.0; http://www.apache.org/licenses/LICENSE-2.0
 * 
 */
public class Text2SpecificAvroPartitionDriver extends Configured implements Tool {
	private static final Log logger = LogFactory.getLog(Text2SpecificAvroPartitionDriver.class);

	public static class Map extends Mapper<LongWritable, Text, AvroKey<EventThreeFieldRecord>, NullWritable> {

		private AvroMultipleOutputs amos;

		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

			long uuid = -1;
			String datetime = "UNK";
			int cat = -1;

			String[] sparts = StringUtils.split(value.toString());
			if (sparts.length==4) {
				uuid = NumberUtils.toLong(sparts[0], -1L);
				datetime = sparts[1]+" "+sparts[2];
				cat = NumberUtils.toInt(sparts[3], -1);
			} else {
				logger.error("ERROR. Unable to create EventThreeFieldRecord from line number "+key+" with contents: "+value);
			}

			EventThreeFieldRecord evrec = EventThreeFieldRecord.newBuilder()
					.setUuid(uuid)
					.setDatetime(datetime)
					.setCategory(cat)
					.build();

			AvroKey<EventThreeFieldRecord> akey = new AvroKey<EventThreeFieldRecord>(evrec);

			amos.write("amono",akey,NullWritable.get(),generateBaseOutputPath(akey.datum().getDatetime()));

		}

		/*
		 * BaseOutputPath for AvroMultipleOutputs
		 * Inputs are DateTime (2015-02-22 23:59:59)
		 * (just splits into Date and Time and calls that function)
		 */
		private String generateBaseOutputPath(CharSequence pDateTime) {
			CharSequence pDate=null;
			CharSequence pTime=null;

			if (StringUtils.isNotBlank(pDateTime) && pDateTime.length()>=19) {
				pDate = pDateTime.subSequence(0, 10);
				pTime = pDateTime.subSequence(11,19);
			}

			return generateBaseOutputPath(pDate, pTime);
		}
		/*
		 * BaseOutputPath for AvroMultipleOutputs
		 * Inputs are Date (2015-06-15) and Time (23:59:59)
		 */
		private String generateBaseOutputPath(CharSequence pDate, CharSequence pTime) {
			/* Output will be something like "pendDate=2015-06-15/pendHour=23/BinRecForPartitions"
			 *    which will generate files like:
			 *    outPath/pendDate=2015-06-15/pendHour=23/BinRecForPartitions-r-00000.avro
			 *    
			 *  Note that partition names can NOT be the same as any column names in schema and
			 *    the partition name MUST be lowercase or else Hive will not use it.
			 */

			if (StringUtils.isBlank(pDate)) {
				pDate="UNK";
			}
			String pHour;
			if (StringUtils.isNotBlank(pTime)) {
				pHour = pTime.toString().substring(0, 2);
			} else {
				pHour = "XX";
			}

			StringBuilder sb = new StringBuilder();

			sb.append("pdate=".toLowerCase());
			sb.append(pDate);
			sb.append("/");
			sb.append("phour=".toLowerCase());
			sb.append(pHour);
			sb.append("/");
			sb.append("eventthreefieldrecord");

			return sb.toString();
		}

		public void setup(Context context) {
			amos = new AvroMultipleOutputs(context);
		}
		public void cleanup(Context context) throws IOException, InterruptedException {
			amos.close();
		}
	}

	public int run(String[] args) throws Exception {

		String input = null;
		String output = null;

		if (args.length != 2) {
			System.err.printf("Usage: %s <input> <output>\n",this.getClass().getSimpleName());
			return -1;
		} else {
			input = args[0];
			output = args[1];
		}

		Job job = Job.getInstance(getConf(), "Text2SpecificAvroPartitionDriver");

		//get schemas
		Schema outSchema = com.awcoleman.examples.avro.EventThreeFieldRecord.getClassSchema();
		job.getConfiguration().set("outSchema",outSchema.toString());

		//Job conf settings
		job.setJarByClass(Text2SpecificAvroPartitionDriver.class);
		job.setMapperClass(Map.class);

		job.setOutputFormatClass(AvroKeyOutputFormat.class);
		job.setMapOutputKeyClass(AvroKeyOutputFormat.class);
		job.setMapOutputValueClass(NullWritable.class);
		AvroJob.setOutputKeySchema(job, outSchema);

		//Create an AvroMultipleOutputs named amono
		AvroMultipleOutputs.addNamedOutput(job, "amono", AvroKeyOutputFormat.class, outSchema, null); 
		//AvroMultipleOutputs.setCountersEnabled(job, true);

		//Set to only Mapper, no Reducer
		job.setNumReduceTasks(0);

		//Job output compression
		FileOutputFormat.setCompressOutput(job, true);
		job.getConfiguration().set(AvroJob.CONF_OUTPUT_CODEC, DataFileConstants.DEFLATE_CODEC);

		//Input and Output Paths (!! LazyOutputFormat is required for proper run output !!)
		FileInputFormat.setInputPaths(job, new Path(input));
		Path outPath = new Path(output);
		FileOutputFormat.setOutputPath(job, outPath);
		LazyOutputFormat.setOutputFormatClass(job, AvroKeyOutputFormat.class);

		return (job.waitForCompletion(true) ? 0 : 1);
	}

	public static void main(String... args) throws Exception {		
		System.exit(ToolRunner.run(new Configuration(), new Text2SpecificAvroPartitionDriver(), args));
	}

}