package com.ycr.framework.common.util;

import com.ycr.framework.common.model.BaseTreeDTO;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TreeUtilsTest {

    /** 测试夹具：id/parentId 型树节点（无需 equals，TreeUtils 用引用同一性即可） */
    static class Node extends BaseTreeDTO<Node, Long> {
        @Getter
        private final String name;

        Node(Long id, Long parentId, String name) {
            setId(id);
            setParentId(parentId);
            this.name = name;
        }
    }

    @Test
    void parseTree_应按父子关系组装嵌套树() {
        List<Node> source = Arrays.asList(
                new Node(1L, null, "root"),
                new Node(2L, 1L, "a"),
                new Node(3L, 1L, "b"),
                new Node(4L, 2L, "a-1")
        );

        List<Node> tree = TreeUtils.parseTree(source, null);

        assertThat(tree).hasSize(1);
        Node root = tree.get(0);
        assertThat(root.getName()).isEqualTo("root");
        assertThat(root.getChildren()).extracting(Node::getName).containsExactly("a", "b");
        Node a = root.getChildren().get(0);
        assertThat(a.getChildren()).extracting(Node::getName).containsExactly("a-1");
        // 叶子节点 children 为 null
        assertThat(root.getChildren().get(1).getChildren()).isNull();
    }

    @Test
    void parseTree_父节点不在集合中时该节点视为顶层() {
        List<Node> source = Arrays.asList(
                new Node(10L, 99L, "orphan"),
                new Node(11L, 10L, "child")
        );

        List<Node> tree = TreeUtils.parseTree(source, null);

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getName()).isEqualTo("orphan");
        assertThat(tree.get(0).getChildren()).extracting(Node::getName).containsExactly("child");
    }

    @Test
    void parseTree_空集合返回空列表() {
        assertThat(TreeUtils.parseTree(Collections.<Node>emptyList(), null)).isEmpty();
        assertThat(TreeUtils.parseTree(null, null)).isEmpty();
        assertThat(TreeUtils.parseTree(Arrays.asList((Node) null, null), null)).isEmpty();
    }

    @Test
    void parseTree_多个孤儿节点应按comparator重排为顶层() {
        // 默认 comparator 按 id 升序；source 故意乱序，验证孤儿被重排而非保持原序
        List<Node> source = Arrays.asList(
                new Node(30L, 999L, "o30"),
                new Node(10L, 999L, "o10"),
                new Node(20L, 999L, "o20")
        );

        List<Node> tree = TreeUtils.parseTree(source, null);

        assertThat(tree).extracting(Node::getName).containsExactly("o10", "o20", "o30");
    }

    @Test
    void parseTree_自定义comparator决定同层顺序() {
        List<Node> source = Arrays.asList(
                new Node(1L, null, "root1"),
                new Node(3L, null, "root3"),
                new Node(2L, null, "root2")
        );

        List<Node> tree = TreeUtils.parseTree(source, Comparator.comparingLong((Node n) -> n.getId()).reversed());

        assertThat(tree).extracting(Node::getName).containsExactly("root3", "root2", "root1");
    }
}
