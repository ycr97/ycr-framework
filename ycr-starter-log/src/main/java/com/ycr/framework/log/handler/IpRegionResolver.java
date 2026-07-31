package com.ycr.framework.log.handler;

/**
 * IP 归属地解析 SPI
 *
 * <p>机制/数据分离：L1 只给接口与默认 no-op 实现，geo 数据（如 ip2region）由业务在 L2/L3 提供并注册为 Bean
 * 覆盖默认。命中 {@code Include.IP_REGION} 且有客户端 IP 时调用。</p>
 *
 * @author ycr
 */
public interface IpRegionResolver {

    /**
     * 解析 IP 归属地
     *
     * @param ip 客户端 IP
     * @return 归属地描述（如「中国-浙江-杭州」）；无法解析返回 null
     */
    String resolve(String ip);
}
