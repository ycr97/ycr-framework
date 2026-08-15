package com.ycr.framework.ddd.aggregate;

import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AggregateTest {

    @Data
    static class Item implements Serializable {
        private Long id;
        private String name;
        private int qty;

        Item(Long id, String name, int qty) {
            this.id = id;
            this.name = name;
            this.qty = qty;
        }
    }

    @Data
    static class Order implements Versionable, Serializable {
        private Long id;
        private int version;
        private String customer;
        private List<Item> items = new ArrayList<>();

        @Override
        public int getVersion() {
            return version;
        }
    }

    private Order newOrder(int version) {
        Order order = new Order();
        order.setId(1L);
        order.setVersion(version);
        order.setCustomer("Alice");
        List<Item> items = new ArrayList<>();
        items.add(new Item(10L, "apple", 2));
        items.add(new Item(20L, "pear", 3));
        order.setItems(items);
        return order;
    }

    @Test
    @DisplayName("新建聚合初始未变更")
    void shouldMatchExpectedBehavior001() {
        Aggregate<Order> aggregate = AggregateFactory.createAggregate(newOrder(1));
        assertThat(aggregate.isChanged()).isFalse();
    }

    @Test
    @DisplayName("version为0时isNew为真")
    void shouldMatchExpectedBehavior002() {
        assertThat(AggregateFactory.createAggregate(newOrder(0)).isNew()).isTrue();
        assertThat(AggregateFactory.createAggregate(newOrder(1)).isNew()).isFalse();
    }

    @Test
    @DisplayName("修改根字段后isChanged为真")
    void shouldMatchExpectedBehavior003() {
        Aggregate<Order> aggregate = AggregateFactory.createAggregate(newOrder(1));
        aggregate.getRoot().setCustomer("Bob");
        assertThat(aggregate.isChanged()).isTrue();
    }

    @Test
    @DisplayName("findNewEntitiesById_识别新增实体")
    void shouldMatchExpectedBehavior004() {
        Aggregate<Order> aggregate = AggregateFactory.createAggregate(newOrder(1));
        aggregate.getRoot().getItems().add(new Item(30L, "grape", 1));

        Collection<Item> created = aggregate.findNewEntitiesById(Order::getItems, Item::getId);

        assertThat(created).extracting(Item::getId).containsExactly(30L);
    }

    @Test
    @DisplayName("findChangedEntities_识别变更实体")
    void shouldMatchExpectedBehavior005() {
        Aggregate<Order> aggregate = AggregateFactory.createAggregate(newOrder(1));
        aggregate.getRoot().getItems().get(0).setQty(99);

        Collection<Item> changed = aggregate.findChangedEntities(Order::getItems, Item::getId);

        assertThat(changed).extracting(Item::getId).containsExactly(10L);
        assertThat(changed.iterator().next().getQty()).isEqualTo(99);
    }

    @Test
    @DisplayName("findChangedEntitiesWithOldValues_返回新旧值")
    void shouldMatchExpectedBehavior006() {
        Aggregate<Order> aggregate = AggregateFactory.createAggregate(newOrder(1));
        aggregate.getRoot().getItems().get(0).setQty(99);

        Collection<ChangedEntity<Item>> changed =
                aggregate.findChangedEntitiesWithOldValues(Order::getItems, Item::getId);

        assertThat(changed).hasSize(1);
        ChangedEntity<Item> ce = changed.iterator().next();
        assertThat(ce.getOldEntity().getQty()).isEqualTo(2);
        assertThat(ce.getNewEntity().getQty()).isEqualTo(99);
    }

    @Test
    @DisplayName("findRemovedEntities_识别删除实体")
    void shouldMatchExpectedBehavior007() {
        Aggregate<Order> aggregate = AggregateFactory.createAggregate(newOrder(1));
        aggregate.getRoot().getItems().removeIf(i -> i.getId().equals(20L));

        Collection<Item> removed = aggregate.findRemovedEntities(Order::getItems, Item::getId);

        assertThat(removed).extracting(Item::getId).containsExactly(20L);
    }
}
