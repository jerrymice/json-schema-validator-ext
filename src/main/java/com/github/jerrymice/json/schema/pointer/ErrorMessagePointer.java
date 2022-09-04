package com.github.jerrymice.json.schema.pointer;

import com.networknt.schema.ValidationMessage;

public interface ErrorMessagePointer {
    /**
     * 是否支持
     *
     * @param pointFactor
     * @return
     */
    boolean isSupport(PointFactor pointFactor);

    /**
     * 获取错误消息路径
     *
     * @return
     */
    String createPointer(ValidationMessage validationMessage);

}
