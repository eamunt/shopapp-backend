package com.project.shopapp.controller;


import com.project.shopapp.components.SecurityUtils;
import com.project.shopapp.dtos.CommentDTO;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.comment.CommentResponse;
import com.project.shopapp.responses.ResponseObject;
import com.project.shopapp.services.comment.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("${api.prefix}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final SecurityUtils securityUtils;

    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllComments(
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam("product_id") Long productId
    ){
        List<CommentResponse> commentResponses;
        if(userId == null) {
            commentResponses = commentService.getCommentsByProduct(productId);
        }else {
            commentResponses = commentService.getCommentsByUserAndProduct(userId, productId);
        }
        return ResponseEntity.ok().body(ResponseObject.builder()
                        .message("Get comments successfully")
                        .status(HttpStatus.OK)
                        .data(commentResponses)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> updateComment(
            @PathVariable("id") Long commentId,
            @Valid @RequestBody CommentDTO commentDTO
    ) throws Exception {
        // kiểm tra người dùng hiện tại
        User loggedInUser = securityUtils.getLoggedInUser();
        if(!Objects.equals(loggedInUser.getId(), commentDTO.getUserId())) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(
                            "You can not update another user's comment",
                            HttpStatus.BAD_REQUEST,
                            null
                    )
            );
        }
        commentService.updateComment(commentId, commentDTO);
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Comment updated successfully")
                .status(HttpStatus.OK)
                .data(commentDTO.getContent())
                .build());
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> insertCommnet(
            @Valid @RequestBody CommentDTO commentDTO
    ){
        User loggedInUser = securityUtils.getLoggedInUser();
        if(!Objects.equals(loggedInUser.getId(), commentDTO.getUserId())) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(
                            "You can not comment as another user",
                            HttpStatus.BAD_REQUEST,
                            null
                    )
            );
        }
        commentService.insertComment(commentDTO);
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Inser Comment Successfully")
                .status(HttpStatus.CREATED)
                .data(commentDTO.getContent())
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> deleteComment(
            @PathVariable("id") Long commentId
    ) throws Exception {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Delete comment successfully")
                .status(HttpStatus.OK)
                .build());
    }


}
