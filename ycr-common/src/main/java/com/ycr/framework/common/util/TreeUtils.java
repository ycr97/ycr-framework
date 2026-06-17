package com.ycr.framework.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ycr.framework.common.model.BaseTreeDTO;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 树构建工具：把扁平列表组装成父子嵌套树。
 *
 * <p>支持两种组织方式：按 {@code id/parentId}（{@link #parseTree}）与按 {@code code/parentCode}
 * （{@link #parseTreeByCode}）。父节点不在集合中的节点会被当作顶层节点，避免数据丢失。</p>
 *
 * @author ycr
 */
public final class TreeUtils {

    private TreeUtils() {
    }

    /**
     * 按 id/parentId 构树。
     *
     * @param source     扁平节点列表
     * @param comparator 同层排序器，为 null 时按 id 数值升序
     * @param <T>        节点类型
     * @param <ID>       主键类型
     * @return 顶层节点列表（children 已递归填充）
     */
    public static <T extends BaseTreeDTO<T, ID>, ID> List<T> parseTree(List<T> source, Comparator<T> comparator) {
        if (CollUtil.isEmpty(source) || source.stream().allMatch(ObjectUtil::isNull)) {
            return Collections.emptyList();
        }
        Comparator<T> cmp = comparator != null ? comparator
                : Comparator.comparingLong(o -> Long.parseLong(String.valueOf(o.getId())));

        List<T> nodes = source.stream().filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        List<T> topNodes = nodes.stream().filter(n -> ObjectUtil.isEmpty(n.getParentId())).sorted(cmp).collect(Collectors.toList());
        List<T> childNodes = nodes.stream().filter(n -> !ObjectUtil.isEmpty(n.getParentId())).sorted(cmp).collect(Collectors.toList());

        Set<ID> ids = nodes.stream().map(BaseTreeDTO::getId).collect(Collectors.toSet());
        for (T node : nodes) {
            if (!ObjectUtil.isEmpty(node.getParentId()) && !ids.contains(node.getParentId())) {
                topNodes.add(node);
                childNodes.remove(node);
            }
        }
        topNodes.sort(cmp);
        if (CollUtil.isEmpty(childNodes)) {
            return topNodes;
        }
        Map<ID, Object> assigned = MapUtil.newHashMap(childNodes.size());
        topNodes.forEach(top -> assembleChild(top, assigned, childNodes));
        return topNodes;
    }

    private static <T extends BaseTreeDTO<T, ID>, ID> void assembleChild(T parent, Map<ID, Object> assigned, List<T> children) {
        List<T> childList = CollUtil.newArrayList();
        children.stream()
                .filter(c -> !assigned.containsKey(c.getId()))
                .filter(c -> c.getParentId().equals(parent.getId()))
                .forEach(c -> {
                    assigned.put(c.getId(), c.getParentId());
                    assembleChild(c, assigned, children);
                    childList.add(c);
                });
        parent.setChildren(CollUtil.isEmpty(childList) ? null : childList);
    }
}
