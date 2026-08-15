package com.ycr.framework.translate.source;

/**
 * 字典数据提供者 SPI（应用侧实现）
 *
 * <p>框架提供字典翻译的<b>机制</b>，但字典数据存储（DB/缓存/配置）只有应用知道，故由应用注册一个
 * {@code DictProvider} Bean 供 {@link DictTranslateSource} 调用。容器中无该 Bean 时字典源不激活。</p>
 *
 * @author ycr
 */
public interface DictProvider {

    /**
     * 按字典编码与项编码取文本标签
     *
     * @param dictCode 字典编码（如 {@code user_status}）
     * @param itemCode 字典项编码（如 {@code 1}）
     * @return 文本标签；不存在返回 {@code null}
     */
    String getLabel(String dictCode, String itemCode);
}
