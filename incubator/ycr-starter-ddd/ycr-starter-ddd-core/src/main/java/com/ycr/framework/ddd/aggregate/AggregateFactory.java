package com.ycr.framework.ddd.aggregate;

/**
 * The aggregate factory will create the aggregate.
 *
 * @author meixuesong
 */
public class AggregateFactory {
    private AggregateFactory() {
        throw new IllegalStateException("A factory class, please use static method");
    }

    private static DeepCopier copier = new SerializableDeepCopier();

    public static <R extends Versionable> Aggregate<R> createAggregate(R root) {
        return new Aggregate<R>(root, copier, new JavaUtilDeepComparator());
    }

    public static void setCopier(DeepCopier copier) {
        AggregateFactory.copier = copier;
    }
}
