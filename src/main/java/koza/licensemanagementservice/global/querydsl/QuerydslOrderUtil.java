package koza.licensemanagementservice.global.querydsl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class QuerydslOrderUtil {
    public static OrderSpecifier<?>[] getOrderSpecifiers(Sort sort, EntityPath<?> parent) {
        return getOrderSpecifiers(sort, parent, "id");
    }

    public static OrderSpecifier<?>[] getOrderSpecifiers(Sort sort, EntityPath<?> parent, String defaultProp) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        PathBuilder<?> pathBuilder = new PathBuilder<>(parent.getType(), parent.getMetadata());

        // 정렬 조건이 없는 경우 기본값 적용
        if (sort.isEmpty()) {
            orders.add(new OrderSpecifier(Order.DESC, pathBuilder.get(defaultProp)));
        } else {
            sort.stream().forEach(order -> {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String prop = order.getProperty();
                orders.add(new OrderSpecifier(direction, pathBuilder.get(prop)));
            });
        }

        return orders.toArray(OrderSpecifier[]::new);
    }
}
