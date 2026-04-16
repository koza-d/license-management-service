package koza.licensemanagementservice.global.querydsl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class QuerydslOrderUtil {
    public static OrderSpecifier<?>[] getOrderSpecifiers(Sort sort, EntityPath<?> parent) {
        return getOrderSpecifiers(sort, parent, "id", Set.of("createAt"));
    }

    public static OrderSpecifier<?>[] getOrderSpecifiers(Sort sort, EntityPath<?> parent, Set<String> allowedProps) {
        return getOrderSpecifiers(sort, parent, "id", allowedProps);
    }

    public static OrderSpecifier<?>[] getOrderSpecifiers(Sort sort, EntityPath<?> parent, String defaultProp, Set<String> allowedProps) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        PathBuilder<?> pathBuilder = new PathBuilder<>(parent.getType(), parent.getMetadata());

        // 정렬 조건이 없는 경우 기본값 적용
        if (sort.isEmpty()) {
            orders.add(new OrderSpecifier(Order.DESC, pathBuilder.get(defaultProp)));
        } else {
            sort.stream().forEach(order -> {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String prop = order.getProperty();
                if (!allowedProps.contains(prop))
                    throw new BusinessException(ErrorCode.INVALID_REQUEST);

                orders.add(new OrderSpecifier(direction, pathBuilder.get(prop)));
            });
        }

        return orders.toArray(OrderSpecifier[]::new);
    }
}
