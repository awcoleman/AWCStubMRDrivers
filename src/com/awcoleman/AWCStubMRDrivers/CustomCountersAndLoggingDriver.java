package com.awcoleman.AWCStubMRDrivers;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/*
 * Driver template to show use of custom counters and logging.
 * 
 * Example is count of lines in text file.
 * 
 * !! Not the most efficient usage to make a better template. Also some unnecessary bits just to jog my memory !!
 * 
 * Example counter counts instances of certain phrase ("grep")
 * 
 * Some sample usage reminders:
 * #Prep dirs
 * hdfs dfs -mkdir -p /tmp/CustomCountersAndLoggingDriver/input
 * 
 * HADOOP_USER_NAME=hdfs hdfs dfs -put /etc/hadoop/conf/* /tmp/CustomCountersAndLoggingDriver/input
 * 
 * cp /etc/inittab /tmp/fileWithPhrase
 * sed -i '1s/^/someWarningToReport /' /tmp/fileWithPhrase
 * hdfs dfs -put -p /tmp/fileWithPhrase /tmp/CustomCountersAndLoggingDriver/input
 * 
 * #Normal
 * export HADOOP_CLASSPATH=${CLASSPATH}
 * export LD_LIBRARY_PATH=/usr/lib/hadoop/lib/native:$LD_LIBRARY_PATH
 * #Run
 * hadoop jar /tmp/CustomCountersAndLoggingDriver.jar com.awcoleman.AWCStubMRDrivers.CustomCountersAndLoggingDriver /tmp/CustomCountersAndLoggingDriver/input /tmp/CustomCountersAndLoggingDriver/output
 * 
 * #For LIBJARS
 * #Prep env
 * #export USERCLASSPATH=/javaLibraries/awc-whatever.jar:/javaLibraries/awc-whatever2.jar
 * #export CLASSPATH=$(hadoop classpath):${USERCLASSPATH}
 * #export HADOOP_CLASSPATH=${CLASSPATH}
 * #export LIBJARS=$(echo ${USERCLASSPATH} | sed s/:/,/g)
 * #export LD_LIBRARY_PATH=/usr/lib/hadoop/lib/native:$LD_LIBRARY_PATH
 * #Run
 * #hadoop jar /tmp/CustomCountersAndLoggingDriver.jar com.awcoleman.AWCStubMRDrivers.CustomCountersAndLoggingDriver -libjars ${LIBJARS} hdfs://localhost:8020/tmp/CustomCountersAndLoggingDriver/input /tmp/CustomCountersAndLoggingDriver/output
 * 
 * #Check output
 * hdfs dfs -ls /tmp/CustomCountersAndLoggingDriver/output
 * hdfs dfs -cat /tmp/CustomCountersAndLoggingDriver/output/part-r-00000
 * hdfs dfs -rm -r /tmp/CustomCountersAndLoggingDriver/output
 * 
 * @author awcoleman
 * license: Apache License 2.0; http://www.apache.org/licenses/LICENSE-2.0
 */
public class CustomCountersAndLoggingDriver extends Configured implements Tool  {
	private static final Log logger = LogFactory.getLog(CustomCountersAndLoggingDriver.class);

	public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);
		private final static Text dummyKey = new Text("D");

		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

			if (value.find("someWarningToReport") != -1) {  //Change to some warning or condition to count
				logger.error("WARNING. The someWarningToReport occurred in Map on line: "+key.toString());
				context.getCounter(COUNTERS.SOME_WARNING_TO_REPORT).increment(1);
			}

			context.write(dummyKey, one);
		}
	}

	public static class Reduce extends Reducer<Text, IntWritable, LongWritable, NullWritable > {
		private LongWritable total = new LongWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			total.set(sum);
			context.write(total, NullWritable.get());
		}
	}

	public static enum COUNTERS {
		SOME_WARNING_TO_REPORT,
		OTHER_ERRORS,
		OTHER_ISSUES
	}

	public int run(String[] args) throws Exception {
		String input = null;
		String output = null;
		if (args.length < 2) {
			System.err.printf("Usage: %s <input> <output>\n",this.getClass().getSimpleName());
			return -1;
		} else {
			input = args[0];
			output = args[1];
		}

		Job job = Job.getInstance(getConf(), "CustomCountersAndLoggingDriver");
		Configuration conf = job.getConfiguration();

		//Job conf settings
		job.setJarByClass(CustomCountersAndLoggingDriver.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);

		//Input and Output Paths
		FileInputFormat.setInputPaths(job, new Path(input));
		FileOutputFormat.setOutputPath(job, new Path(output));

		boolean jobCompletionStatus = job.waitForCompletion(true);

		//Print Custom Counters before exiting (will be with normal job information)
		Counters counters = job.getCounters();
		for (COUNTERS customCounter : COUNTERS.values()) {
			Counter thisCounter = counters.findCounter(customCounter);
			System.out.println("Custom Counter "+customCounter+"="+thisCounter.getValue());
		}

		return jobCompletionStatus ? 0 : 1;
	}

	public static void main(String... args) throws Exception {		
		System.exit(ToolRunner.run(new Configuration(), new CustomCountersAndLoggingDriver(), args));
	}
}
