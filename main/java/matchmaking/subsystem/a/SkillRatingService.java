package matchmaking.subsystem.a;
import matchmaking.MatchmakingException;
import java.util.List;
public class SkillRatingService {
    public int averageRating(List<Integer> ratings) {
        if (ratings.size() != 10) {
            throw new MatchmakingException("exactly 10 ratings are required.");
        }
        int sum = 0;
        for (Integer rating : ratings) {
            sum += rating;
        }
        return sum / ratings.size();
    }
}