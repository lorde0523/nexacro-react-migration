// [MIGRATION] AS-IS: Nexcore Service - BoardService
// [TO-BE]: Spring @Service

package com.migration.nexacro.service;

import com.migration.nexacro.dto.BoardDTO;
import com.migration.nexacro.dto.SearchCondition;
import com.migration.nexacro.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 게시판 서비스 (Nexcore Service → Spring @Service)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;

    /**
     * 게시글 목록 조회 (Nexacro fn_search 대응)
     */
    @Transactional(readOnly = true)
    public List<BoardDTO> getBoardList(SearchCondition condition) {
        log.debug("게시글 목록 조회: {}", condition);
        return boardMapper.findAll(condition);
    }

    /**
     * 게시글 상세 조회 (조회수 증가 포함)
     */
    @Transactional
    public BoardDTO getBoardById(Long boardId) {
        log.debug("게시글 상세 조회: boardId={}", boardId);
        BoardDTO board = boardMapper.findById(boardId);
        if (board == null) {
            throw new RuntimeException("게시글을 찾을 수 없습니다: " + boardId);
        }
        boardMapper.incrementViewCount(boardId);
        return board;
    }

    /**
     * 게시글 등록 (Nexacro INSERT 대응)
     * Nexacro WebEditor getInnerHTML() → content 필드에 HTML 저장
     */
    @Transactional
    public BoardDTO createBoard(BoardDTO board) {
        log.debug("게시글 등록: {}", board.getTitle());
        boardMapper.insert(board);
        return board;
    }

    /**
     * 게시글 수정 (Nexacro UPDATE 대응)
     */
    @Transactional
    public BoardDTO updateBoard(Long boardId, BoardDTO board) {
        log.debug("게시글 수정: boardId={}", boardId);
        board.setBoardId(boardId);
        int updated = boardMapper.update(board);
        if (updated == 0) {
            throw new RuntimeException("게시글을 찾을 수 없습니다: " + boardId);
        }
        return board;
    }

    /**
     * 게시글 삭제 (논리 삭제, Nexacro DELETE 대응)
     */
    @Transactional
    public void deleteBoard(Long boardId) {
        log.debug("게시글 삭제: boardId={}", boardId);
        int deleted = boardMapper.deleteById(boardId);
        if (deleted == 0) {
            throw new RuntimeException("게시글을 찾을 수 없습니다: " + boardId);
        }
    }
}
