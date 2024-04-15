package com.flhai.myrpc.core.governance;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * SlidingTimeWindow implement based on RingBuffer and TS(timestamp).
 * Use TS/1000->SecondNumber to mapping an index slot in a RingBuffer.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2022-11-20 19:39:27
 */
@ToString
@Slf4j
public class SlidingTimeWindow {

    public static final int DEFAULT_SIZE = 30;

    // 窗口大小，以秒为单位，表示窗口可以覆盖的时间跨度。
    private final int size;
    private final RingBuffer ringBuffer;
    private int sum = 0;

    //    private int _start_mark = -1;
    //    private int _prev_mark  = -1;
    // 在环形缓冲区中当前秒的索引位置。
    private int _curr_mark = -1;

    // 窗口开始的时间戳（秒）
    private long _start_ts = -1L;
    //   private long _prev_ts  = -1L;
    // 当前记录的最新时间戳（秒）。
    private long _curr_ts = -1L;

    public SlidingTimeWindow() {
        this(DEFAULT_SIZE);
    }

    public SlidingTimeWindow(int _size) {
        this.size = _size;
        this.ringBuffer = new RingBuffer(this.size);
    }

    /**
     * 记录方法，用于处理新的时间戳（以毫秒为单位）。这个方法将时间戳转换为秒，并更新环形缓冲区和相关的统计数据。
     * 如果是第一次记录，调用initRing初始化环形缓冲区。
     * 如果新的时间戳与当前时间戳相同，只增加当前时间戳对应的计数。
     * 如果新时间戳在当前时间戳之后但仍在窗口大小内，将重置当前时间戳到新时间戳之间的位置，并更新新时间戳的计数。
     * 如果新时间戳超出了窗口大小范围，将重置整个环形缓冲区，并以新时间戳重新初始化环形缓冲区。
     *
     * @param millis
     */
    public synchronized void record(long millis) {
        log.debug("window before: " + this.toString());
        log.debug("window.record(" + millis + ")");
        long ts = millis / 1000;
        if (_start_ts == -1L) {
            initRing(ts);
        } else {   // TODO  Prev 是否需要考虑
            if (ts == _curr_ts) {
                log.debug("window ts:" + ts + ", curr_ts:" + _curr_ts + ", size:" + size);
                this.ringBuffer.incr(_curr_mark, 1);
            } else if (ts > _curr_ts && ts < _curr_ts + size) {
                int offset = (int) (ts - _curr_ts);
                log.debug("window ts:" + ts + ", curr_ts:" + _curr_ts + ", size:" + size + ", offset:" + offset);
                this.ringBuffer.reset(_curr_mark + 1, offset);
                this.ringBuffer.incr(_curr_mark + offset, 1);
                _curr_ts = ts;
                _curr_mark = (_curr_mark + offset) % size;
            } else if (ts >= _curr_ts + size) {
                log.debug("window ts:" + ts + ", curr_ts:" + _curr_ts + ", size:" + size);
                this.ringBuffer.reset();
                initRing(ts);
            }
        }
        this.sum = this.ringBuffer.sum();
        log.debug("window after: " + this.toString());
    }

    /**
     * 初始化环形缓冲区，将开始时间和当前时间设为给定的时间戳，增加该时间戳位置的计数。
     * @param ts
     */
    private void initRing(long ts) {
        log.debug("window initRing ts:" + ts);
        this._start_ts = ts;
        this._curr_ts = ts;
        this._curr_mark = 0;
        this.ringBuffer.incr(0, 1);
    }

    public int getSize() {
        return size;
    }

    public int getSum() {
        return sum;
    }

    public RingBuffer getRingBuffer() {
        return ringBuffer;
    }

    public int get_curr_mark() {
        return _curr_mark;
    }

    public long get_start_ts() {
        return _start_ts;
    }

    public long get_curr_ts() {
        return _curr_ts;
    }

}