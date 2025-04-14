package com.projectmanagement.userservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(Model model) {
        // Nếu cần truyền thêm dữ liệu, ví dụ thông tin người dùng
        model.addAttribute("username", "Tên người dùng");
        return "home";  // Trang chủ chính
    }

    @GetMapping("/profile")
    public String profile() {
        return "users/profile";  // Đường dẫn tới trang profile.html
    }
}
