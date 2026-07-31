package com.ycr.framework.common.util;

import com.ycr.framework.common.model.BaseTreeCodeDTO;
import com.ycr.framework.common.model.BaseTreeDTO;
import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("parseTree_应按父子关系组装嵌套树")
    void shouldMatchExpectedBehavior001() {
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
    @DisplayName("parseTree_父节点不在集合中时该节点视为顶层")
    void shouldMatchExpectedBehavior002() {
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
    @DisplayName("parseTree_空集合返回空列表")
    void shouldMatchExpectedBehavior003() {
        assertThat(TreeUtils.parseTree(Collections.<Node>emptyList(), null)).isEmpty();
        assertThat(TreeUtils.parseTree(null, null)).isEmpty();
        assertThat(TreeUtils.parseTree(Arrays.asList((Node) null, null), null)).isEmpty();
    }

    @Test
    @DisplayName("parseTree_多个孤儿节点应按comparator重排为顶层")
    void shouldMatchExpectedBehavior004() {
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
    @DisplayName("parseTree_自定义comparator决定同层顺序")
    void shouldMatchExpectedBehavior005() {
        List<Node> source = Arrays.asList(
                new Node(1L, null, "root1"),
                new Node(3L, null, "root3"),
                new Node(2L, null, "root2")
        );

        List<Node> tree = TreeUtils.parseTree(source, Comparator.comparingLong((Node n) -> n.getId()).reversed());

        assertThat(tree).extracting(Node::getName).containsExactly("root3", "root2", "root1");
    }

    /** 测试夹具：code/parentCode 型树节点 */
    static class CodeNode extends BaseTreeCodeDTO<CodeNode, String> {
        @Getter
        private final String name;

        CodeNode(String code, String parentCode, String name) {
            setCode(code);
            setParentCode(parentCode);
            this.name = name;
        }
    }

    @Test
    @DisplayName("parseTreeByCode_应按编码父子关系组装嵌套树")
    void shouldMatchExpectedBehavior006() {
        List<CodeNode> source = Arrays.asList(
                new CodeNode("A", null, "root"),
                new CodeNode("A01", "A", "a"),
                new CodeNode("A0101", "A01", "a-1")
        );

        List<CodeNode> tree = TreeUtils.parseTreeByCode(source, null);

        assertThat(tree).hasSize(1);
        CodeNode root = tree.get(0);
        assertThat(root.getName()).isEqualTo("root");
        assertThat(root.getChildren()).extracting(CodeNode::getName).containsExactly("a");
        assertThat(root.getChildren().get(0).getChildren()).extracting(CodeNode::getName).containsExactly("a-1");
    }

    @Test
    @DisplayName("parseTreeByCode_空集合返回空列表")
    void shouldMatchExpectedBehavior007() {
        assertThat(TreeUtils.parseTreeByCode(Collections.<CodeNode>emptyList(), null)).isEmpty();
        assertThat(TreeUtils.parseTreeByCode(null, null)).isEmpty();
        assertThat(TreeUtils.parseTreeByCode(Arrays.asList((CodeNode) null, null), null)).isEmpty();
    }

    @Test
    @DisplayName("parseTreeByCode_父节点不在集合中时该节点视为顶层")
    void shouldMatchExpectedBehavior008() {
        List<CodeNode> source = Arrays.asList(
                new CodeNode("X01", "X", "orphan"),
                new CodeNode("X0101", "X01", "child")
        );

        List<CodeNode> tree = TreeUtils.parseTreeByCode(source, null);

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getName()).isEqualTo("orphan");
        assertThat(tree.get(0).getChildren()).extracting(CodeNode::getName).containsExactly("child");
    }

    @Test
    @DisplayName("parseTreeByCode_自定义comparator决定同层顺序")
    void shouldMatchExpectedBehavior009() {
        List<CodeNode> source = Arrays.asList(
                new CodeNode("A", null, "rootA"),
                new CodeNode("C", null, "rootC"),
                new CodeNode("B", null, "rootB")
        );

        List<CodeNode> tree = TreeUtils.parseTreeByCode(source, Comparator.comparing((CodeNode n) -> n.getCode()).reversed());

        assertThat(tree).extracting(CodeNode::getName).containsExactly("rootC", "rootB", "rootA");
    }
}
