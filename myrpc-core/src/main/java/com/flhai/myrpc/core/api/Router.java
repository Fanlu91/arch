package com.flhai.myrpc.core.api;

import java.util.List;

public interface Router<InstanceMeta> {
    List<InstanceMeta> route(List<InstanceMeta> providers);

    Router Default = providers -> providers;
}
