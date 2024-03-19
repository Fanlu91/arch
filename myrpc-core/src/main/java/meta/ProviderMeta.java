package meta;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class ProviderMeta {
    Method method;
    String signName;
    Object serviceImpl;
}
