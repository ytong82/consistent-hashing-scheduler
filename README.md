# consistent-hashing-scheduler
A scheduler based on consistent hashing algorithm

代码在scheduler文件夹里

scheduler.imbalance.factor 
调整调度器的公平比，如果是1的话就算极端情况，round robin平均分布，尽最大可能的让每个服务器上的任务的权重之和一样大。
如果是一个比较大的数，比如大于2，甚至是10的情况，调度器不再是一个公平分配器，这样任务粘性，即同一种类型的任务需要尽量被同一台机器执行将占据主要地位。

scheduler.bound.load.threshold.factor
粘性系数，如果粘性系数很小，比如是1，甚至是0，5，那么任务会尽量在一个小的机器范围内调度和分配，这样任务粘性，即同一种类型的任务需要尽量被同一台机器执行就会高。
如果粘性系数高，比如和任务种类数接近，那么任务会在整个机器群的范围内调度和分配，这样任务粘性就会低。
如果粘性系数很小，因为任务会尽量在一个小的机器范围内调度和分配，所以会出现更多数量的未被使用的机器。

如果调小scheduler.imbalance.factor和scheduler.bound.load.threshold.factor会让调度器运行更长的时间。
