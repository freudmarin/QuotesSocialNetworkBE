package com.marin.quotesdashboardbackend.services.recommendation;


import com.marin.quotesdashboardbackend.dtos.DTOMappings;
import com.marin.quotesdashboardbackend.dtos.QuoteDTO;
import com.marin.quotesdashboardbackend.entities.Quote;
import com.marin.quotesdashboardbackend.entities.User;
import com.marin.quotesdashboardbackend.entities.UserQuoteInteraction;
import com.marin.quotesdashboardbackend.repositories.QuoteRepository;
import com.marin.quotesdashboardbackend.repositories.UserQuoteInteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemBasedCollaborativeFiltering {

    private final UserQuoteInteractionRepository interactionRepository;
    private final QuoteRepository quoteRepository;

    public List<QuoteDTO> recommendQuotes(User user) {
        List<Quote> likedQuotes = interactionRepository.findByUserAndLikedTrue(user)
                .stream()
                .map(UserQuoteInteraction::getQuote)
                .toList();

        List<UserQuoteInteraction> allInteractions = interactionRepository.findAllLikedInteractions();

        // Map of quotes to users who liked them
        Map<Quote, Set<User>> quoteToUsersMap = new HashMap<>();
        for (UserQuoteInteraction interaction : allInteractions) {
            quoteToUsersMap
                    .computeIfAbsent(interaction.getQuote(), k -> new HashSet<>())
                    .add(interaction.getUser());
        }

        Map<Quote, Double> quoteScores = new HashMap<>();
        for (Quote quote : quoteRepository.findAllWithTagsAndAuthors()) {
            if (!likedQuotes.contains(quote)) {
                double score = 0.0;
                for (Quote likedQuote : likedQuotes) {
                    score += calculateCosineSimilarity(quote, likedQuote, quoteToUsersMap);
                }
                quoteScores.put(quote, score);
            }
        }

        var quotesToRecommend = quoteScores.entrySet().stream()
                .sorted(Map.Entry.<Quote, Double>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .map(DTOMappings::fromQuoteToQuoteDTO)
                .toList();
        log.info("Recommended quotes in item-based collaborative filtering: {}", quotesToRecommend.size());
        return quotesToRecommend;
    }

    private double calculateCosineSimilarity(Quote quote1, Quote quote2, Map<Quote, Set<User>> quoteToUsersMap) {
        Set<User> users1 = quoteToUsersMap.getOrDefault(quote1, Collections.emptySet());
        Set<User> users2 = quoteToUsersMap.getOrDefault(quote2, Collections.emptySet());

        Set<User> allUsers = new HashSet<>(users1);
        allUsers.addAll(users2);

        int[] vector1 = new int[allUsers.size()];
        int[] vector2 = new int[allUsers.size()];

        int index = 0;
        for (User user : allUsers) {
            vector1[index] = users1.contains(user) ? 1 : 0;
            vector2[index] = users2.contains(user) ? 1 : 0;
            index++;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < allUsers.size(); i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
