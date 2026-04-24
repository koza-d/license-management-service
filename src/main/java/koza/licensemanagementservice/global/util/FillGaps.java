package koza.licensemanagementservice.global.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FillGaps {
    public static <T> List<T> fillDateGaps(LocalDate from,
                                     LocalDate to,
                                     List<T> dbResults,
                                     Function<T, LocalDate> dateExtractor,
                                     Function<LocalDate, T> defaultFactory) {
        Map<LocalDate, T> resultMap = dbResults.stream()
                .collect(Collectors.toMap(dateExtractor, r -> r));

        List<T> filledList = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            filledList.add(resultMap.getOrDefault(date, defaultFactory.apply(date)));
        }

        return filledList;
    }
}
