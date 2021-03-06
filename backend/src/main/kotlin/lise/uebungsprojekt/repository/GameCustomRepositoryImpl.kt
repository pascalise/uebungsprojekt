package lise.uebungsprojekt.repository

import lise.uebungsprojekt.model.Game
import lise.uebungsprojekt.model.GameDetail
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.*
import org.springframework.data.mongodb.core.aggregation.Aggregation.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository


@Repository
class GameCustomRepositoryImpl(@Autowired val mongoTemplate: MongoTemplate): GameCustomRepository {
    override fun getGameById(id: ObjectId): GameDetail? {
        val agg: Aggregation = this.buildAggregation(id, listOf(), "")
        val results: List<GameDetail> = mongoTemplate.aggregate(agg, "game", GameDetail::class.java)
                .mappedResults
        if(results.isNotEmpty()) {
            return results[0]
        }
        return null
    }

    override fun findAll(filterByConsoles: List<String>, searchBy: String): List<Game> {
        val agg: Aggregation = this.buildAggregation(null, filterByConsoles, searchBy)
        return mongoTemplate.aggregate(agg, "game", Game::class.java).mappedResults
    }

    private fun buildAggregation(id: ObjectId? = null, filterByConsoles: List<String>, searchBy: String): Aggregation {
        val ratingsLookup = lookup("rating", "_id", "gameId", "ratings")
        val consoleLookup = lookup("console", "consoles", "_id", "consoles")
        val avgGraphicsRatingOp = buildAddFieldsOperation("avgGraphicsRating", "ratings.graphics")
        val avgSoundRatingOp = buildAddFieldsOperation("avgSoundRating", "ratings.sound")
        val avgAddictionRatingOp = buildAddFieldsOperation("avgAddictionRating", "ratings.addiction")
        val avgActionRatingOp = buildAddFieldsOperation("avgActionRating", "ratings.action")
        return if (id != null) {
            newAggregation(match(Criteria("_id").`is`(id)), consoleLookup, ratingsLookup, avgGraphicsRatingOp,
                avgSoundRatingOp, avgAddictionRatingOp, avgActionRatingOp)
        } else {
            val filterByConsolesOp = match(Criteria.where("consoles")
                .elemMatch(Criteria.where("name").`in`(*filterByConsoles.toTypedArray())))
            val searchNameOp = match(Criteria.where("name").regex(".*$searchBy.*", "i"))
            val sortOp = sort(Sort.Direction.DESC, "averageRating", "releaseDate")
            if(filterByConsoles.isNotEmpty()) {
                newAggregation(searchNameOp, consoleLookup, filterByConsolesOp, ratingsLookup, avgGraphicsRatingOp,
                    avgSoundRatingOp, avgAddictionRatingOp, avgActionRatingOp, sortOp)
            } else {
                newAggregation(searchNameOp, consoleLookup, ratingsLookup, avgGraphicsRatingOp,
                    avgSoundRatingOp, avgAddictionRatingOp, avgActionRatingOp, sortOp)
            }
        }
    }

    private fun buildAddFieldsOperation(field: String, fieldReference: String): AddFieldsOperation {
        return AddFieldsOperation.builder().addField(field).withValueOf(ArithmeticOperators.valueOf(fieldReference)
            .avg()).build()
    }
}
