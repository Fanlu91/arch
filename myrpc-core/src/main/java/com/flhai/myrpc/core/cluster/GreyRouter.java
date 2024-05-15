package com.flhai.myrpc.core.cluster;

import com.flhai.myrpc.core.api.Router;
import com.flhai.myrpc.core.meta.InstanceMeta;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class GreyRouter implements Router<InstanceMeta> {
    @Getter
    @Setter
    private int greyRate;


    public GreyRouter(int greyRate) {
        this.greyRate = greyRate;
    }

    /**
     * 灰度路由
     * 根据灰度比例，按照概率返回纯净节点或灰度节点
     * 这样避免LB的算法不支持灰度节点的情况，即不要求LB算法线性且均匀
     *
     * @param providers
     * @return
     */
    @Override
    public List<InstanceMeta> route(List<InstanceMeta> providers) {
        Random random = new Random();

        if (providers == null || providers.size() < 2) {
            return providers;
        }

        List<InstanceMeta> normalNodes = new ArrayList<>();
        List<InstanceMeta> greyNodes = new ArrayList<>();
        for (InstanceMeta provider : providers) {
            if (provider.getParameters().get("grey").equals("true")) {
                greyNodes.add(provider);
            } else {
                normalNodes.add(provider);
            }
        }

        if (normalNodes.isEmpty() || greyNodes.isEmpty()) {
            return providers;
        }
//        if (greyRate <= 0) {
//            return normalNodes;
//        }
//        if (greyRate >= 100) {
//            return greyNodes;
//        }

        // 假设LoadBalance的算法是线性均匀分布的
        if (random.nextInt(100) <= greyRate) {
            return greyNodes;
        } else {
            return normalNodes;
        }
    }
}
