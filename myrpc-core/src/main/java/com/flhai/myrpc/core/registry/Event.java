package com.flhai.myrpc.core.registry;

import com.flhai.myrpc.core.meta.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Event {
    List<InstanceMeta> instance;
}
