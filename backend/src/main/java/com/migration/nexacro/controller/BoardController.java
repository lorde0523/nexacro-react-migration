// [MIGRATION] AS-IS: Nexcore .xpc 서비스 정의 - 게시판 서비스
// [TO-BE]: Spring @RestController

package com.migration.nexacro.controller;

import com.migration.nexacro.dto.BoardDTO;
import com.migration.nexacro.dto.SearchCondition;
import com.migration.nexacro.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게시판 REST Controller (Nexcore .xpc 서비스 → @RestController)
 *
 * Nexacro Transaction 매핑:
 * - GET    /api/boards       → 게시글 목록 조회 (fn_search)
 * - GET    /api/boards/{id}  → 게시글 상세 조회
 * - POST   /api/boards       → 게시글 등록 (btn_save_onclick)
 * - PUT    /api/boards/{id}  → 게시글 수정 (btn_save_onclick)
 * - DELETE /api/boards/{id}  → 게시글 삭제 (btn_delete_onclick)
 *
 * Nexacro WebEditor 변환:
 * - getInnerHTML() → content 필드에 HTML 저장/조회
 */
@Slf4j
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * 게시글 목록 조회 (Nexacro fn_search 대응)
     */
    @GetMapping
    public ResponseEntity<List<BoardDTO>> getBoardList(SearchCondition condition) {
        log.info("게시글 목록 조회 요청: {}", condition);
        List<BoardDTO> boards = boardService.getBoardList(condition);
        return ResponseEntity.ok(boards);
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDTO> getBoardById(@PathVariable Long boardId) {
        log.info("게시글 상세 조회 요청: boardId={}", boardId);
        BoardDTO board = boardService.getBoardById(boardId);
        return ResponseEntity.ok(board);
    }

    /**
     * 게시글 등록 (Nexacro btn_save_onclick - INSERT 대응)
     */
    @PostMapping
    public ResponseEntity<BoardDTO> createBoard(@RequestBody BoardDTO board) {
        log.info("게시글 등록 요청: {}", board.getTitle());
        BoardDTO created = boardService.createBoard(board);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 게시글 수정 (Nexacro btn_save_onclick - UPDATE 대응)
     */
    @PutMapping("/{boardId}")
    public ResponseEntity<BoardDTO> updateBoard(
            @PathVariable Long boardId,
            @RequestBody BoardDTO board) {
        log.info("게시글 수정 요청: boardId={}", boardId);
        BoardDTO updated = boardService.updateBoard(boardId, board);
        return ResponseEntity.ok(updated);
    }

    /**
     * 게시글 삭제 (Nexacro btn_delete_onclick 대응)
     */
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardId) {
        log.info("게시글 삭제 요청: boardId={}", boardId);
        boardService.deleteBoard(boardId);
        return ResponseEntity.noContent().build();
    }
}
