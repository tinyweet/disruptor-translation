package com.lmax.disruptor.dsl;

import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;

import java.util.concurrent.Executor;

/**
 * 消费者信息
 */
interface ConsumerInfo
{
	/**
	 * 获取消费者拥有的所有的序列，消费者的消费进度由最小的Sequence决定
	 * 一个消费者可能有多个Sequence，它的好处在于可以保持简单性，减少使用同一个Sequence的交互/竞争。
	 * eg:WorkPool构成的消费者就有多个Sequence
	 *
	 * 消费者之间的可见性保证：
	 * 后继消费者观察其前驱消费者的进度来保证可见性。
	 * Sequence是单调递增的，当看见前驱消费者的进度增大时，所有前驱消费者对区间段内的数据的处理对后置消费者来说都是可见的。
	 * volatile的happens-before原则-----前驱消费者们的进度变大(写volatile)先于我看见它变大(读volatile)。
	 *
	 * 注意：相同的可见性策略---与Sequence之间交互的消费者之间的可见性保证。
	 * {@link com.lmax.disruptor.AbstractSequencer#gatingSequences}
	 *
	 * {@link com.lmax.disruptor.Sequencer#getHighestPublishedSequence(long, long)}
	 *
	 * @return
	 */
    Sequence[] getSequences();

	/**
	 * 获取当前消费者持有的序列屏障
	 *
	 * 每一个消费者有且仅有一个Barrier屏障，该屏障用于协调当前消费者与它依赖的Sequence所属的消费者们之间的速度。
	 * 消费者依赖的Sequence使指它的所有直接前驱节点的Sequence。
	 *
	 * 什么是消费者依赖的Sequence？ {@link com.lmax.disruptor.ProcessingSequenceBarrier#dependentSequence}
	 *
	 * {@link com.lmax.disruptor.Sequencer#newBarrier(Sequence...)}
	 * @return
	 */
    SequenceBarrier getBarrier();

	/**
	 * 当前消费者是否是消费链末端的消费者(没有后继消费者)
	 * 如果是末端的消费者，那么它就是生产者关注的消费者对象
	 *
	 * {@link com.lmax.disruptor.Sequencer#addGatingSequences(Sequence...)}
	 * {@link com.lmax.disruptor.Sequencer#removeGatingSequence(Sequence)}}
	 * {@link com.lmax.disruptor.AbstractSequencer#gatingSequences}
	 * @return
	 */
    boolean isEndOfChain();

	/**
	 * 启动消费者。
	 * 主要是为每一个{@link com.lmax.disruptor.EventProcessor}创建线程,启动事件监听
	 * @param executor
	 */
    void start(Executor executor);

	/**
	 * 通知消费者处理完当前事件之后，停止下来
	 * 类似线程中断或任务的取消操作
	 * {@link java.util.concurrent.Future#cancel(boolean)}
	 * {@link Thread#interrupt()}
	 */
    void halt();

	/**
	 * 当我新增了后继消费者的时候，标记为不是消费者链末端的消费者
	 * 生产者就不在需要对我保持关注
	 */
    void markAsUsedInBarrier();

	/**
	 * 消费者当前是否正在运行
	 * @return
	 */
	boolean isRunning();
}
