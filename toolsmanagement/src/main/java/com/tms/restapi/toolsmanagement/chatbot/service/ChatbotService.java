package com.tms.restapi.toolsmanagement.chatbot.service;

import com.tms.restapi.toolsmanagement.chatbot.dto.ChatbotQADTO;
import com.tms.restapi.toolsmanagement.chatbot.dto.ChatbotResponseDTO;
import com.tms.restapi.toolsmanagement.chatbot.model.ChatbotQA;
import com.tms.restapi.toolsmanagement.chatbot.repository.ChatbotQARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ChatbotService {

    @Autowired
    private ChatbotQARepository chatbotQARepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Process user query and return answer
     * Checks predefined Q&A first, then performs dynamic database search
     */
    public ChatbotResponseDTO processQuery(String query) {
        long startTime = System.currentTimeMillis();
        
        if (query == null || query.trim().isEmpty()) {
            return new ChatbotResponseDTO(
                query,
                false,
                "Query cannot be empty. Please provide a valid question."
            );
        }

        // Step 1: Check for exact match in predefined Q&A
        Optional<ChatbotQA> exactMatch = chatbotQARepository.findByQuestionExact(query.trim());
        if (exactMatch.isPresent()) {
            long endTime = System.currentTimeMillis();
            ChatbotResponseDTO response = new ChatbotResponseDTO(
                query,
                exactMatch.get().getAnswer(),
                "predefined",
                endTime - startTime,
                true
            );
            return response;
        }

        // Step 2: Check for partial match in predefined Q&A
        List<ChatbotQA> partialMatches = chatbotQARepository.findByQuestionKeyword(query.trim());
        if (!partialMatches.isEmpty()) {
            long endTime = System.currentTimeMillis();
            ChatbotResponseDTO response = new ChatbotResponseDTO(
                query,
                partialMatches.get(0).getAnswer(),
                "predefined",
                endTime - startTime,
                true
            );
            return response;
        }

        // Step 3: Perform dynamic search across database tables
        String dynamicAnswer = performDynamicSearch(query);
        long endTime = System.currentTimeMillis();

        if (dynamicAnswer != null && !dynamicAnswer.isEmpty()) {
            ChatbotResponseDTO response = new ChatbotResponseDTO(
                query,
                dynamicAnswer,
                "dynamic",
                endTime - startTime,
                true
            );
            return response;
        }

        // Step 4: No answer found
        return new ChatbotResponseDTO(
            query,
            false,
            "I couldn't find relevant information for your query. Please try rephrasing your question or check the available data in the system."
        );
    }

    /**
     * Perform dynamic search across database tables
     * Searches in tools, issuance, trainers, admins, and other related tables
     */
    private String performDynamicSearch(String query) {
        String lowerQuery = query.toLowerCase();
        StringBuilder result = new StringBuilder();

        try {
            // Search in tools table
            String toolsSql = "SELECT description, tool_no, siNo, location, availability, tool_condition FROM tools " +
                            "WHERE LOWER(description) LIKE ? OR LOWER(tool_no) LIKE ? OR LOWER(siNo) LIKE ? LIMIT 5";
            List<Map<String, Object>> toolResults = jdbcTemplate.queryForList(
                toolsSql,
                "%" + lowerQuery + "%",
                "%" + lowerQuery + "%",
                "%" + lowerQuery + "%"
            );
            
            if (!toolResults.isEmpty()) {
                result.append("Tools found: ");
                for (Map<String, Object> tool : toolResults) {
                    result.append(tool.get("description")).append(" (Tool No: ")
                            .append(tool.get("tool_no")).append(", Availability: ")
                            .append(tool.get("availability")).append("), ");
                }
                return result.toString();
            }

            // Search in issuance table
            String issuanceSql = "SELECT tool_id, issued_by, issued_to, status, issued_date FROM issuance " +
                               "WHERE LOWER(issued_to) LIKE ? OR LOWER(status) LIKE ? LIMIT 5";
            List<Map<String, Object>> issuanceResults = jdbcTemplate.queryForList(
                issuanceSql,
                "%" + lowerQuery + "%",
                "%" + lowerQuery + "%"
            );
            
            if (!issuanceResults.isEmpty()) {
                result.append("Issuance records found: ");
                for (Map<String, Object> issuance : issuanceResults) {
                    result.append("Issued to: ").append(issuance.get("issued_to"))
                            .append(", Status: ").append(issuance.get("status"))
                            .append(", Date: ").append(issuance.get("issued_date")).append("; ");
                }
                return result.toString();
            }

            // Search in trainer table
            String trainerSql = "SELECT name, email, specialization FROM trainer " +
                              "WHERE LOWER(name) LIKE ? OR LOWER(specialization) LIKE ? LIMIT 5";
            List<Map<String, Object>> trainerResults = jdbcTemplate.queryForList(
                trainerSql,
                "%" + lowerQuery + "%",
                "%" + lowerQuery + "%"
            );
            
            if (!trainerResults.isEmpty()) {
                result.append("Trainers found: ");
                for (Map<String, Object> trainer : trainerResults) {
                    result.append(trainer.get("name")).append(" (")
                            .append(trainer.get("specialization")).append("), ");
                }
                return result.toString();
            }

            // Search in admin table
            String adminSql = "SELECT name, email, location FROM admin " +
                            "WHERE LOWER(name) LIKE ? OR LOWER(location) LIKE ? LIMIT 5";
            List<Map<String, Object>> adminResults = jdbcTemplate.queryForList(
                adminSql,
                "%" + lowerQuery + "%",
                "%" + lowerQuery + "%"
            );
            
            if (!adminResults.isEmpty()) {
                result.append("Admins found: ");
                for (Map<String, Object> admin : adminResults) {
                    result.append(admin.get("name")).append(" (")
                            .append(admin.get("location")).append("), ");
                }
                return result.toString();
            }

        } catch (Exception e) {
            // If table doesn't exist or query fails, continue
            // This allows flexibility as the system grows
        }

        return null;
    }

    /**
     * Get all predefined Q&A
     */
    public List<ChatbotQADTO> getAllQAs() {
        List<ChatbotQA> qaList = chatbotQARepository.findAllActiveQAs();
        return qaList.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Add new predefined Q&A
     */
    public ChatbotQADTO addQA(ChatbotQADTO qaDTO) {
        ChatbotQA qa = new ChatbotQA(qaDTO.getQuestion(), qaDTO.getAnswer());
        ChatbotQA saved = chatbotQARepository.save(qa);
        return convertToDTO(saved);
    }

    /**
     * Update existing Q&A
     */
    public ChatbotQADTO updateQA(Long id, ChatbotQADTO qaDTO) {
        Optional<ChatbotQA> existing = chatbotQARepository.findById(id);
        
        if (existing.isPresent()) {
            ChatbotQA qa = existing.get();
            qa.setQuestion(qaDTO.getQuestion());
            qa.setAnswer(qaDTO.getAnswer());
            ChatbotQA updated = chatbotQARepository.save(qa);
            return convertToDTO(updated);
        }
        
        return null;
    }

    /**
     * Delete Q&A (soft delete)
     */
    public boolean deleteQA(Long id) {
        Optional<ChatbotQA> existing = chatbotQARepository.findById(id);
        
        if (existing.isPresent()) {
            ChatbotQA qa = existing.get();
            qa.setIsActive(false);
            chatbotQARepository.save(qa);
            return true;
        }
        
        return false;
    }

    /**
     * Convert ChatbotQA entity to DTO
     */
    private ChatbotQADTO convertToDTO(ChatbotQA qa) {
        return new ChatbotQADTO(
            qa.getId(),
            qa.getQuestion(),
            qa.getAnswer(),
            qa.getCreatedAt(),
            qa.getUpdatedAt(),
            qa.getIsActive()
        );
    }
}
