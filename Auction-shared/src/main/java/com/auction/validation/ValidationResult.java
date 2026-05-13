package com.auction.validation;
import java.util.*;

public class ValidationResult {
    //field -> danh sách lỗi của field đó
    //fieldErrors sẽ lưu hết tất cả các lỗi của mọi thuộc tính theo thuộc tính
    //Lưu trữ một cách tiện lợi và dễ gọi ra lỗi của từng thuộc tính
    private final Map<String, String> fieldErrors;

    //constructor, không cho phép sửa đổi sau khi tạo
    public ValidationResult(Map<String, String> Errors) {
        this.fieldErrors = Collections.unmodifiableMap(Errors);
    }
    //Nếu không có lỗi trả về Map rỗng (không cho phép sửa sau khi đã tạo)
    public static ValidationResult ok(){
        return new ValidationResult(Map.of());
    }
    /*
    LinkedHashMap là một implementation đặc biệt của Map = HashMap + LinkedList(lưu thứ tự)
    Không giống như HashMap (vì lưu giữ theo HashCode nên có thể mất đi thứ tự khi duyệt)
    LinkedHashMap có thể lưu được thứ tự Key (nhờ linkedlist lưu hộ thứ tự key)
    Mục đích, ta có thể lưu lại các message lỗi theo field và lỗi nào xét đến đầu tiên thì chỉ hiện mỗi lỗi đó
     */
    public static ValidationResult from(List<FieldError> errors){
        //Tạo map từ danh sách các FieldError
        Map<String,String> map = new LinkedHashMap<>();
        for (FieldError fielderror : errors){
            // Tự kiểm tra và tạo cặp key-value
            map.put(fielderror.field(),fielderror.message());
        }
        return new ValidationResult(map);
        }
        //Lưu ý: 1 lần một field chỉ được map với một lỗi
    /*
    Nếu không có tin nhắn lỗi nào thì hợp lệ
    */
    public boolean valid(){
        return fieldErrors.isEmpty();
    }
    //Controller gọi cái này để bôi đỏ đúng textfield

    //Nếu có lỗi thì message lỗi
    //Nếu không có lỗi thì return empty String (không được phép modify
    public String errorFor(String field){
        return fieldErrors.getOrDefault(field,"");
    }

    //Kiểm tra xem Field có lỗi hay không
    public boolean hasErrorFor(String field){
        return fieldErrors.containsKey(field);
    }

    }

