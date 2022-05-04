package lise.uebungsprojekt.repository

import lise.uebungsprojekt.model.Game
import lise.uebungsprojekt.model.GameBase
import lise.uebungsprojekt.model.GameDetail
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.LookupOperation
import org.springframework.data.mongodb.core.aggregation.MatchOperation
import org.springframework.data.mongodb.core.aggregation.SortOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository

@Repository
class GameCustomRepositoryImpl(@Autowired val mongoTemplate: MongoTemplate): GameCustomRepository {
    override fun getGameById(id: ObjectId): GameDetail? {
        val agg: Aggregation = this.buildAggregation(id)
        val results: List<GameDetail> = mongoTemplate.aggregate(agg, "game", GameDetail::class.java)
                .mappedResults
        if(results.isNotEmpty()) {
            return results[0]
        }
        return null
    }

    override fun findAll(): List<Game> {
        val agg: Aggregation = this.buildAggregation()
        return mongoTemplate.aggregate(agg, "game", Game::class.java).mappedResults
    }

    private fun buildAggregation(id: ObjectId? = null): Aggregation {
        val ratingsLookup: LookupOperation =
                Aggregation.lookup("rating", "_id", "gameId", "ratings")
        val consoleLookup: LookupOperation =
                Aggregation.lookup("console", "consoles", "_id", "consoles")
        val matchIdOp: MatchOperation = Aggregation.match(Criteria("_id").`is`(id))
        return if (id != null) {
            Aggregation.newAggregation(matchIdOp, consoleLookup, ratingsLookup)
        } else {
            val sortOp: SortOperation = Aggregation.sort(Sort.Direction.DESC, "releaseDate")
            Aggregation.newAggregation(consoleLookup, ratingsLookup, sortOp)
        }
    }
}
