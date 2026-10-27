package cn.lili.common.security.sensitive;

import cn.lili.common.properties.SystemSettingProperties;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.security.sensitive.enums.SensitiveStrategy;
import cn.lili.common.utils.SpringContextUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Objects;

/**
 * 敏感信息序列化时 过滤
 *
 * @author liushuai(liushuai711 @ gmail.com)
 * @version v4.0
 * @Description:
 * @since 2021/9/10 16:46
 */
public class SensitiveJsonSerializer extends StdSerializer<String> implements ContextualSerializer {

    private SensitiveStrategy strategy;

    public SensitiveJsonSerializer() {
        super(String.class);
    }

    private SensitiveJsonSerializer(SensitiveStrategy strategy) {
        super(String.class);
        this.strategy = strategy;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(strategy.desensitizer().apply(value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (desensitization()) {
            Sensitive annotation = property.getAnnotation(Sensitive.class);
            if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
                return new SensitiveJsonSerializer(annotation.strategy());
            }
        }
        return prov.findValueSerializer(property.getType());
    }

    /**
     * 是否需要脱敏处理
     */
    private boolean desensitization() {
        AuthUser authUser = UserContext.getCurrentUser();
        if (authUser == null) {
            return false;
        }

        SystemSettingProperties systemSettingProperties = SpringContextUtil.getBean(SystemSettingProperties.class);

        if (authUser.getRole().equals(UserEnums.STORE)) {
            return systemSettingProperties.getSensitiveLevel() == 2;
        }

        if (authUser.getRole().equals(UserEnums.MANAGER)) {
            return systemSettingProperties.getSensitiveLevel() >= 1;
        }

        return false;
    }
}