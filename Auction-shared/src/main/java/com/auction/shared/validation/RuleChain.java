package com.auction.shared.validation;

import java.util.List;
import java.util.Optional;

public class RuleChain<T> {
    private final List<ValidationRule<T>> rules;
    //Kiểm tra giá trị bị lỗi gì
    //Varargs ... sẽ để cho java tự tạo giúp mình một array với các phần tử tham số truyền vào.
    //Chủ yếu là giúp gọi gọn hơn
    @SafeVarargs
    public RuleChain(ValidationRule<T>... rules) {
        this.rules =  List.of(rules); //Tạo ra list từ Array rules (tham số truyền vào)
    }
    public Optional<String> run (T value){
        for (ValidationRule<T> rule : rules){
            Optional<String> error = rule.check(value);
            if (error.isPresent()) {
                return error; //Dừng ngay
            }
        }
        return Optional.empty(); //Tất cả pass
    }

}
