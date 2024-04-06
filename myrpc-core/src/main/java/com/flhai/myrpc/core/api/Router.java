package com.flhai.myrpc.core.api;

import com.flhai.myrpc.core.meta.InstanceMeta;

import java.util.List;

public interface Router {
    List<InstanceMeta> route(List<InstanceMeta> providers);

    Router Default = providers -> providers;
}
