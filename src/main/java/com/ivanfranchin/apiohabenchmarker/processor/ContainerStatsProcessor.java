package com.ivanfranchin.apiohabenchmarker.processor;

public interface ContainerStatsProcessor extends Runnable {

  void stop();

  double getMaxCpuUsage();

  double getMaxMemUsage();
}
