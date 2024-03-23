package com.flhai.myrpc.core.api;

import java.util.List;

public interface Router {
    List<String> route(List<String> providers);

    Router Default = providers -> providers;
}
