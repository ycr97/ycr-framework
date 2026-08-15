/**
 * 聚合持久化：对聚合根做快照（深拷贝）并与当前值深比较，识别新增/变更/删除实体与字段级 delta，
 * 支撑仓储层精准持久化。
 *
 * <p>本包移植自开源项目 meixuesong/aggregate-persistence 与 cedarsoftware/java-util（{@code deepequals} 子包），
 * 均为 Apache License 2.0；移植仅做包名适配，逻辑保持一致，故类上保留原作者署名。</p>
 *
 * @author meixuesong
 */
package com.ycr.framework.ddd.aggregate;
