package com.sagares.saga_res_api.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        String nextCursor,
        boolean hasNext
) {
    // Đảm bảo content không bao giờ bị null khi trả về JSON
    public PageResponse {
        content = (content == null) ? List.of() : List.copyOf(content);
    }

    //Function<T, String> cursorExtractor đóng vai trò như một hàm trích xuất: nhận vào một đối tượng kiểu T (ví dụ: một bài viết, đơn hàng, tin nhắn)
    // và trả về một chuỗi String chính là giá trị dùng làm con trỏ (cursor) của đối tượng đó.
    public static <T> PageResponse<T> of(List<T> items, int limit, Function<T, String> cursorExtractor) {
        //Kiểm tra xem còn phần tử tiếp theo không
        boolean hasNext = items.size() > limit;
        //Cắt phần tử phù hợp size
        List<T> pageContent = hasNext ? items.subList(0, limit) : items;
        //Gán giá trị nextCursor
        String nextCursor = hasNext ? cursorExtractor.apply(pageContent.getLast()) : null;
        //return
        return new PageResponse<>(pageContent, nextCursor, hasNext);
    }

    //Hàm tạo cursor cho lần gọi request tiếp theo, truyền createAt, id
    public static String formatCursor(LocalDateTime createdAt, long id) {
        Cursor cursor = new Cursor(createdAt, id);
        return cursor.createdAt() + "_" + cursor.id();
    }
    //Hàm từ value cursor parse lại thành cursor
    public static Cursor parseCursor(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Giá trị cursor là bắt buộc");
        }
        String[] keys = value.split("_", -1);
        if (keys.length != 2) {
            throw new IllegalArgumentException("Không đúng format cursor createdAt_ID");
        }

        try {
            return new Cursor(LocalDateTime.parse(keys[0]), Long.parseLong(keys[1]));
        } catch (DateTimeParseException | NumberFormatException ex) {
            throw new IllegalArgumentException("Khong đúng định dạng của ngày tạo v id", ex);
        }
    }

    public record Cursor(LocalDateTime createdAt, long id) {
        public Cursor {
            if (createdAt == null || createdAt.getYear() < 1 || createdAt.getYear() > 9999 || id <= 0) {
                throw new IllegalArgumentException("cursor vượt quá phạm vi");
            }
        }
    }
}