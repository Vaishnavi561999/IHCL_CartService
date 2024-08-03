package com.ihcl.cart.repository

import com.ihcl.cart.config.MongoConfig
import com.ihcl.cart.model.schema.Cart
import com.ihcl.cart.model.schema.GiftCardCart
import com.ihcl.cart.model.schema.GiftCardValues
import com.ihcl.cart.model.schema.LoyaltyCart
import com.mongodb.BasicDBObject
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.UpdateOptions
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class CartRepository {
    private var cartCollection: CoroutineCollection<Cart> = MongoConfig.getDatabase().getCollection()
    private var gcCartCollection: CoroutineCollection<GiftCardCart> = MongoConfig.getDatabase().getCollection()
    private var loyaltyCartCollection: CoroutineCollection<LoyaltyCart> = MongoConfig.getDatabase().getCollection()
    private val giftCardCollection: CoroutineCollection<GiftCardValues> = MongoConfig.getDatabase().getCollection()
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    suspend fun saveCart(cart: Cart) {
        cart.modifiedTimestamp = Date()
        cartCollection.save(cart)
    }

    suspend fun findCartByCustomerHash(customerHash: String): Cart? {
        return cartCollection.findOne(Cart::_id eq customerHash)
    }

    suspend fun findCartAndUpdate(customerHash: String?, cart: Cart?) {
        val updateObject = BasicDBObject()
        updateObject["\$set"] = cart
        cartCollection.findOneAndUpdate(Cart::_id eq customerHash, updateObject, FindOneAndUpdateOptions().upsert(true))
    }

    suspend fun updateCart(customerHash: String, cart: Cart) {
        cartCollection.updateOneById(customerHash, cart, UpdateOptions().upsert(true))
    }
    suspend fun updateCartForLoyalty(customerHash: String, cart: LoyaltyCart) {
        loyaltyCartCollection.updateOneById(customerHash, cart, UpdateOptions().upsert(true))
    }
    suspend fun updateCartForGC(customerHash: String, cart: GiftCardCart) {
        gcCartCollection.updateOneById(customerHash, cart, UpdateOptions().upsert(true))
    }

    suspend fun deleteBookingCart(id:String) {
        val result = cartCollection.deleteOneById(id)
        log.info("deleted item $result")
    }
    suspend fun deleteCartForLoyalty(id:String) {
        val result = loyaltyCartCollection.deleteOneById(id)
        log.info("deleted item $result")
    }
    suspend fun deleteCartForGC(id:String) {
        val result = gcCartCollection.deleteOneById(id)
        log.info("deleted item $result")
    }

    suspend fun saveGiftCardCart(gcCart: GiftCardCart) {
        gcCart.modifiedTimestamp = Date()
        gcCartCollection.save(gcCart)
    }

    suspend fun fetchGiftCardByCustomerHash(customerHash: String): GiftCardCart? {
        return gcCartCollection.findOne(GiftCardCart::_id eq customerHash)
    }
    suspend fun findGiftCardCartByCustomerHash(customerHash: String):GiftCardCart?{
        return gcCartCollection.findOne(GiftCardCart::_id eq customerHash)
    }
    suspend fun updateGCCart(customerHash: String, gcCart: GiftCardCart) {
        gcCartCollection.updateOneById(customerHash, gcCart, UpdateOptions().upsert(true))
    }
    suspend fun getGiftCardValues(cartId: String): GiftCardValues? {
        return giftCardCollection.findOneById(cartId)
    }

    suspend fun saveLoyaltyCart(loyaltyCart: LoyaltyCart) {
        loyaltyCart.modifiedTimestamp = Date()
        loyaltyCartCollection.save(loyaltyCart)
    }
    suspend fun findLoyaltyCartByCustomerHash(customerHash: String): LoyaltyCart? {
        return loyaltyCartCollection.findOne(LoyaltyCart::_id eq customerHash)
    }
}

