package com.thinkaurelius.faunus.mapreduce;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configuration.IntegerRanges;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.MapContext;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.security.Credentials;

/**
 * MemoryMapper supports in-memory mapping for a chain of consecutive mappers.
 * This provides significant performance improvements as each map need not write its results to disk.
 * Note that MemoryMapper is not general-purpose and is specific to Faunus' current MapReduce library.
 * In particular, it assumes that the chain of mappers emits 0 or 1 key/value pairs for each input.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MemoryMapper<A, B, C, D> extends Mapper<A, B, C, D> {

    public class MemoryMapContext extends Mapper.Context {

        private static final String DASH = "-";
        private static final String EMPTY = "";

        private final Configuration currentConfiguration = new Configuration();

        private Writable key = null;
        private Writable value = null;
        private Writable tempKey = null;
        private Writable tempValue = null;
        private Mapper.Context context;
        private Configuration globalConfiguration;

        public MemoryMapContext(final Mapper.Context context) throws IOException, InterruptedException {
            super();
            this.context = context;
            this.globalConfiguration = context.getConfiguration();
        }

        @Override
        public void write(final Object key, final Object value) throws IOException, InterruptedException {
            this.key = (Writable) key;
            this.value = (Writable) value;
        }

        @Override
        public Writable getCurrentKey() {
            this.tempKey = this.key;
            this.key = null;
            return this.tempKey;
        }

        @Override
        public Writable getCurrentValue() {
            this.tempValue = this.value;
            this.value = null;
            return tempValue;
        }

        @Override
        public boolean nextKeyValue() {
            return this.key != null && this.value != null;
        }

        @Override
        public Counter getCounter(final String groupName, final String counterName) {
            return this.context.getCounter(groupName, counterName);
        }

        @Override
        public Counter getCounter(final Enum counterName) {
            return this.context.getCounter(counterName);
        }

        @Override
        public Configuration getConfiguration() {
            return this.currentConfiguration;
        }

        public InputSplit getInputSplit() {
            return this.context.getInputSplit();
        }

        public void setContext(final Mapper.Context context) {
            this.context = context;
        }

        public void stageConfiguration(final int step) {
            this.currentConfiguration.clear();
            for (final Map.Entry<String, String> entry : this.globalConfiguration) {
                final String key = entry.getKey();
                if (key.endsWith(DASH + step)) {
                    this.currentConfiguration.set(key.replace(DASH + step, EMPTY), entry.getValue());
                } else if (!key.matches(".*-[0-9]+")) {
                    this.currentConfiguration.set(key, entry.getValue());
                }
            }
        }

        @Override
        public String getStatus() {
            return this.context.getStatus();
        }

        @Override
        public TaskAttemptID getTaskAttemptID() {
            return this.context.getTaskAttemptID();
        }

        @Override
        public void setStatus(String msg) {
            this.context.setStatus(msg);
        }

        @Override
        public Path[] getArchiveClassPaths() {
            return this.context.getArchiveClassPaths();
        }

        @Override
        public String[] getArchiveTimestamps() {
            return this.context.getArchiveTimestamps();
        }

        @Override
        public URI[] getCacheArchives() throws IOException {
            return this.context.getCacheArchives();
        }

        @Override
        public URI[] getCacheFiles() throws IOException {
            return this.context.getCacheArchives();
        }

        @Override
        public Class<? extends Reducer<?, ?, ?, ?>> getCombinerClass()
            throws ClassNotFoundException {
            return this.context.getCombinerClass();
        }

        @Override
        public Path[] getFileClassPaths() {
            return this.context.getFileClassPaths();
        }

        @Override
        public String[] getFileTimestamps() {
            return this.context.getFileTimestamps();
        }

        @Override
        public RawComparator<?> getGroupingComparator() {
            return this.context.getGroupingComparator();
        }

        @Override
        public Class<? extends InputFormat<?, ?>> getInputFormatClass()
            throws ClassNotFoundException {
            return this.context.getInputFormatClass();
        }

        @Override
        public String getJar() {
            return this.context.getJar();
        }

        @Override
        public JobID getJobID() {
            return this.context.getJobID();
        }

        @Override
        public String getJobName() {
            return this.context.getJobName();
        }

        @Override
        public boolean userClassesTakesPrecedence() {
            return this.context.userClassesTakesPrecedence();
        }

        @Override
        public boolean getJobSetupCleanupNeeded() {
            return this.context.getJobSetupCleanupNeeded();
        }

        @Override
        public Path[] getLocalCacheArchives() throws IOException {
            return this.context.getLocalCacheArchives();
        }

        @Override
        public Path[] getLocalCacheFiles() throws IOException {
            return this.context.getLocalCacheFiles();
        }

        @Override
        public Class<?> getMapOutputKeyClass() {
            return this.context.getMapOutputKeyClass();
        }

        @Override
        public Class<?> getMapOutputValueClass() {
            return this.context.getMapOutputValueClass();
        }

        @Override
        public Class<? extends Mapper<?, ?, ?, ?>> getMapperClass()
            throws ClassNotFoundException {
            return this.context.getMapperClass();
        }

        @Override
        public int getMaxMapAttempts() {
            return this.context.getMaxMapAttempts();
        }

        @Override
        public int getMaxReduceAttempts() {
            return this.context.getMaxReduceAttempts();
        }

        @Override
        public int getNumReduceTasks() {
            return this.context.getNumReduceTasks();
        }

        @Override
        public Class<? extends OutputFormat<?, ?>> getOutputFormatClass()
            throws ClassNotFoundException {
            return this.context.getOutputFormatClass();
        }

        @Override
        public Class<?> getOutputKeyClass() {
            return this.context.getOutputKeyClass();
        }

        @Override
        public Class<?> getOutputValueClass() {
            return this.context.getOutputValueClass();
        }

        @Override
        public Class<? extends Partitioner<?, ?>> getPartitionerClass()
            throws ClassNotFoundException {
            return this.context.getPartitionerClass();
        }

        @Override
        public Class<? extends Reducer<?, ?, ?, ?>> getReducerClass()
            throws ClassNotFoundException {
            return this.context.getReducerClass();
        }

        @Override
        public RawComparator<?> getSortComparator() {
            return this.context.getSortComparator();
        }

        @Override
        public boolean getSymlink() {
            return this.context.getSymlink();
        }

        @Override
        public Path getWorkingDirectory() throws IOException {
            return this.context.getWorkingDirectory();
        }

        @Override
        public void progress() {
            this.context.progress();
        }

        @Override
        public boolean getProfileEnabled() {
            return this.context.getProfileEnabled();
        }

        @Override
        public String getProfileParams() {
            return this.context.getProfileParams();
        }

        @Override
        public String getUser() {
            return this.context.getUser();
        }

        @Override
        public Credentials getCredentials() {
            return this.context.getCredentials();
        }

        @Override
        public OutputCommitter getOutputCommitter() {
            return this.context.getOutputCommitter();
        }
    }
}
