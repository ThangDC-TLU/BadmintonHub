package com.badmintonhub.reviewservice.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import java.util.function.Function;

@Getter @Setter @JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectResponse<T> {
    private Meta meta; private T result;
    @Getter @Setter public static class Meta {
        private int page; private int pageSize; private int pages; private long total; private boolean hasNext; private boolean hasPrev; private String sort;
    }
    public static <E,R> ObjectResponse<R> fromPage(Page<E> page, String sort, Function<Page<E>,R> payload){
        var m=new Meta(); m.setPage(page.getNumber()+1); m.setPageSize(page.getSize()); m.setTotal(page.getTotalElements());
        m.setPages(Math.max(1,page.getTotalPages())); m.setHasNext(page.hasNext()); m.setHasPrev(page.hasPrevious()); m.setSort(sort);
        var r=new ObjectResponse<R>(); r.setMeta(m); r.setResult(payload.apply(page)); return r;
    }
}
