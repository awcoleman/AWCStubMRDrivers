package com.awcoleman.AWCStubMRDrivers;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileConstants;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.FileReader;
import org.apache.avro.file.SeekableInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapred.AvroValue;
import org.apache.avro.mapred.FsInput;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyInputFormat;
import org.apache.avro.mapreduce.AvroKeyOutputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/*
 * Driver template for avro to avro MapReduce job using GenericRecord
 * 
 * Example is just rewrites input records. Map and Reduce do unnecessary work to be good templates for normal counting,altering,etc.
 * 
 * A sample avro file is at https://github.com/apache/spark/blob/master/examples/src/main/resources/users.avro
 * 
 * export USERCLASSPATH=/usr/lib/avro/avro-mapred-hadoop2.jar
 * export CLASSPATH=$(hadoop classpath):${USERCLASSPATH}
 * export HADOOP_CLASSPATH=${CLASSPATH}
 * export LIBJARS=$(echo ${USERCLASSPATH} | sed s/:/,/g)
 * export LD_LIBRARY_PATH=/usr/lib/hadoop/lib/native:$LD_LIBRARY_PATH
 * 
 * hadoop jar /tmp/Drivers.jar com.awcoleman.AWCStubMRDrivers.Avro2AvroGenericDriver -libjars ${LIBJARS} /tmp/Avro2AvroGenericDriver/input /tmp/Avro2AvroGenericDriver/output
 * 
 * #Check output
 * hdfs dfs -ls /tmp/Avro2AvroGenericDriver/output
 * avro-tools tojson hdfs://localhost:8020/tmp/Avro2AvroGenericDriver/output/part-r-00000.avro
 * hdfs dfs -rm -r /tmp/Avro2AvroGenericDriver/output
 * 
 * @author awcoleman
 * license: Apache License 2.0; http://www.apache.org/licenses/LICENSE-2.0
 */
public class Avro2AvroGenericDriver extends Configured implements Tool {

	public static class Avro2AvroGenericDriverMapper extends Mapper<AvroKey<GenericRecord>, NullWritable, AvroKey<Long>, AvroValue<GenericRecord>> {
		public void map(AvroKey<GenericRecord> key, NullWritable value, Context context) throws IOException, InterruptedException {			

			//Switch GenericRecord inside this AvroKey to wrapped in AvroValue instead
			GenericRecord grec = (GenericRecord)key.datum();
			AvroValue<GenericRecord> aval = new AvroValue<GenericRecord>(grec);
			//AvroKey<Long> akey = new AvroKey<Long>( (long)((Integer)((GenericRecord)grec.get("cdrHeader")).get("recordType")).longValue() );
			AvroKey<Long> akey = new AvroKey<Long>(1L);
			context.write(akey, aval);
		}
	}

	public static class Avro2AvroGenericDriverReducer extends Reducer<AvroKey<Long>, AvroValue<GenericRecord>, AvroKey<GenericRecord>, NullWritable> {
		public void reduce(AvroKey<Long> key, Iterable<AvroValue<GenericRecord>> values, Context context) throws IOException, InterruptedException {

			for (AvroValue<GenericRecord> thisValue : values) {
				GenericRecord grec = (GenericRecord)thisValue.datum();
				AvroKey<GenericRecord> akey = new AvroKey<GenericRecord>(grec);

				context.write(akey, NullWritable.get() );
			}
		}
	}

	//Return first avro schema found in files in input dir
	public static Schema getInOutSchemaFromData(String input, Configuration conf)
	{
		try
		{
			String dataFileName=null;
			FileSystem fs = FileSystem.get(conf);
			FileStatus[] fileStatus = fs.listStatus(new Path(input));
			for(FileStatus status : fileStatus) {
				dataFileName=status.getPath().toString();

				SeekableInput seekin = new FsInput(new Path(dataFileName), conf);
				DatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>();
				FileReader<GenericRecord> fileReader = DataFileReader.openReader(seekin, reader);
				Schema thisSchema = fileReader.getSchema();
				fileReader.close();
				if (thisSchema!=null) return thisSchema;
			}
		}
		catch (Exception e)
		{
			return null;
		}
		return null;
	}
	
	public int run(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.printf("Usage: %s <input> <output>\n",this.getClass().getSimpleName());
			return -1;
		}
		Job job = Job.getInstance(getConf(), "Avro2AvroGenericDriver");
		Configuration conf = job.getConfiguration();

		Schema schema = getInOutSchemaFromData(args[0],conf);

		job.setJarByClass(Avro2AvroGenericDriver.class);
		job.setJobName("Avro2AvroGenericDriver");

		job.setInputFormatClass(AvroKeyInputFormat.class);

		job.setMapperClass(Avro2AvroGenericDriverMapper.class);
		AvroJob.setMapOutputKeySchema(job, Schema.create(Schema.Type.LONG));

		if (schema==null) {
			System.err.printf("Output schema required and not found in files in input directory. Exiting.\n",this.getClass().getSimpleName());
			return -1;
		}
		AvroJob.setMapOutputValueSchema(job, schema);

		job.setReducerClass(Avro2AvroGenericDriverReducer.class);

		AvroJob.setOutputKeySchema(job, schema);
		job.setOutputFormatClass(AvroKeyOutputFormat.class);

		FileOutputFormat.setCompressOutput(job, true);
		job.getConfiguration().set(AvroJob.CONF_OUTPUT_CODEC, DataFileConstants.DEFLATE_CODEC);

		Path inputPath = new Path(args[0]);
		Path outputPath = new Path(args[1]);
		FileInputFormat.addInputPath(job, inputPath);
		FileOutputFormat.setOutputPath(job, outputPath);

		return (job.waitForCompletion(true) ? 0 : 1);
	}

	public static void main(String... args) throws Exception {
		System.exit(ToolRunner.run(new Configuration(), new Avro2AvroGenericDriver(), args));
	}
}