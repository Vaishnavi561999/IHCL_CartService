package com.ihcl.cart.service

import com.ihcl.cart.config.Configuration
import com.ihcl.cart.model.dto.request.*
import com.ihcl.cart.model.exception.HttpResponseException
import com.ihcl.cart.model.schema.Cart
import com.ihcl.cart.repository.CartRepository
import com.ihcl.cart.service.v1.CartService
import com.ihcl.cart.utils.CONFIG_URL
import com.ihcl.cart.utils.DataMapperUtils
import com.ihcl.cart.utils.ValidatorUtils
import io.ktor.server.config.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.util.*
import kotlin.test.*

class CartServiceTest {
/*

    val validatorUtils = mockk<ValidatorUtils>()
    val dataMapperUtils = mockk<DataMapperUtils>()
    val cartRepository = mockk<CartRepository>(relaxed = true)

    @BeforeTest
    fun setup() {
        val envVariables = arrayOf(CONFIG_URL)
        if (checkEnv(envVariables)) {
            val configURL: String = System.getenv(CONFIG_URL)
            Configuration.initConfig(configURL)
        }
        stopKoin()
        startKoin {
            this.modules(
                module {
                    single { cartRepository }
                }
            )
        }
    }

    @DisplayName("it should return successful update cart message when cart not null")
    @Test
    fun updateCartSuccess() {
        val productOutlineElement: ProductOutlineElement = ProductOutlineElement(
            "229869",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            "https://media.croma.com/image/upload/v1605269918/Croma%20Assets/Communication/Mobiles/Images/8999510310942.png",
            84900.0,
            69900.0,
            18,
            "18%",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            true,
            0.0,
            1.0,
            "2022-07-26T17:35:25.233+05:30",
            true,
            ""
        )

        var products = listOf(productOutlineElement)
        var productOutline: ProductOutline = ProductOutline(products)

        var updateCartRequest = CartRequest(
            "229869", 69900.0, 84900.0, 69900.0, "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5", 1, "302015", "electronics", productOutline
        )
        var cartProductDto = CartProductDto(
            "229869",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            "electronics",
            1,
            "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            84900.0,
            69900.0,
            69900.0,
            "302015",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            Date(),
            Date()
        )
        val cartProductDtoList = mutableListOf<CartProductDto>(cartProductDto)
        val cart = Cart("dsd2", cartProductDtoList, Date(), Date())
        coEvery {
            validatorUtils.validateQuantity(productOutlineElement.availableQty.toInt(), updateCartRequest.quantity)
        }
        coEvery {
            cartRepository.findCartByCustomerHash("d54918c2aeda310f2d3a21c2f2570ac3")
        } returns cart

        coEvery {
            cartRepository.saveCart(cart)
        } returns mockk()

        val sut = CartService();
        runBlocking {
            var response = sut.updateCart("d54918c2aeda310f2d3a21c2f2570ac3", updateCartRequest)
            assertEquals("Update Cart Successful", response)
        }

    }


    @DisplayName("it should return successful update cart message when cart null")
    @Test
    fun updateCartSuccesswithCartNull() {
        val productOutlineElement: ProductOutlineElement = ProductOutlineElement(
            "229869",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            "https://media.croma.com/image/upload/v1605269918/Croma%20Assets/Communication/Mobiles/Images/8999510310942.png",
            84900.0,
            69900.0,
            18,
            "18%",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            true,
            0.0,
            1.0,
            "2022-07-26T17:35:25.233+05:30",
            true,
            ""
        )

        var products = listOf(productOutlineElement)
        var productOutline: ProductOutline = ProductOutline(products)

        var updateCartRequest = CartRequest(
            "229869", 69900.0, 84900.0, 69900.0, "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5", 1, "302015", "electronics", productOutline
        )


        coEvery {
            validatorUtils.validateQuantity(productOutlineElement.availableQty.toInt(), updateCartRequest.quantity)
        }
        var cartProductDto = CartProductDto(
            "229869",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            "electronics",
            1,
            "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            84900.0,
            69900.0,
            69900.0,
            "302015",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            Date(),
            Date()
        )
        val cartProductDtoList = mutableListOf<CartProductDto>(cartProductDto)
        val cart = Cart("d54918c2aeda310f2d3a21c2f2570ac3", cartProductDtoList, Date(), Date())

        coEvery {
            cartRepository.findCartByCustomerHash("d54918c2aeda310f2d3a21c2f2570ac3")
        } returns null

        val mock = spyk<CartService>(recordPrivateCalls = true)
        coEvery {
            mock["addInitialProduct"]("d54918c2aeda310f2d3a21c2f2570ac3", updateCartRequest)
        } returns cart

        coEvery {
            cartRepository.saveCart(cart)
        } returns Unit


        val sut = CartService();
        runBlocking {
            var response = sut.updateCart("d54918c2aeda310f2d3a21c2f2570ac3", updateCartRequest)
            assertEquals("Update Cart Successful", response)
        }

    }

    @DisplayName("it should throw error message when skuId is not found")
    @Test
    fun updateCartSuccesswithCartWhenSkuIdNotFound() {
        val productOutlineElement: ProductOutlineElement = ProductOutlineElement(
            "229869",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            "https://media.croma.com/image/upload/v1605269918/Croma%20Assets/Communication/Mobiles/Images/8999510310942.png",
            84900.0,
            69900.0,
            18,
            "18%",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            true,
            0.0,
            1.0,
            "2022-07-26T17:35:25.233+05:30",
            true,
            ""
        )

        var products = listOf(productOutlineElement)
        var productOutline: ProductOutline = ProductOutline(products)

        var updateCartRequest = CartRequest(
            "229869", 69900.0, 84900.0, 69900.0, "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5", 1, "302015", "electronics", productOutline
        )


        coEvery {
            validatorUtils.validateQuantity(productOutlineElement.availableQty.toInt(), updateCartRequest.quantity)
        }
        var cartProductDto = CartProductDto(
            "229860",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            "electronics",
            1,
            "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            84900.0,
            69900.0,
            69900.0,
            "302015",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            Date(),
            Date()
        )
        val cartProductDtoList = mutableListOf<CartProductDto>(cartProductDto)
        val cart = Cart("d54918c2aeda310f2d3a21c2f2570ac3", cartProductDtoList, Date(), Date())

        coEvery {
            cartRepository.findCartByCustomerHash("d54918c2aeda310f2d3a21c2f2570ac3")
        } returns cart


        val sut = CartService();
        runBlocking {

            val exception = assertFailsWith<HttpResponseException>(
                block = { sut.updateCart("d54918c2aeda310f2d3a21c2f2570ac3", updateCartRequest) }
            )
            assertEquals("SkuId is not present in cart", exception.data)

        }

    }


    @DisplayName("it should return successful add to cart message when cart not null")
    @Test
    fun addToCartSuccess() {
        val productOutlineElement: ProductOutlineElement = ProductOutlineElement(
            "229869",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            "https://media.croma.com/image/upload/v1605269918/Croma%20Assets/Communication/Mobiles/Images/8999510310942.png",
            84900.0,
            69900.0,
            18,
            "18%",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            true,
            0.0,
            1.0,
            "2022-07-26T17:35:25.233+05:30",
            true,
            ""
        )

        var products = listOf(productOutlineElement)
        var productOutline: ProductOutline = ProductOutline(products)

        var addtoCartRequest = CartRequest(
            "229869", 69900.0, 84900.0, 69900.0, "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5", 1, "302015", "electronics", productOutline
        )
        var cartProductDto = CartProductDto(
            "229869",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            "electronics",
            0,
            "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            84900.0,
            69900.0,
            69900.0,
            "302015",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            Date(),
            Date()
        )
        val cartProductDtoList = mutableListOf<CartProductDto>(cartProductDto)
        val cart = Cart("d54918c2aeda310f2d3a21c2f2570ac3", cartProductDtoList, Date(), Date())

        coEvery {
            cartRepository.findCartByCustomerHash("d54918c2aeda310f2d3a21c2f2570ac3")
        } returns cart

        coEvery {
            cartRepository.saveCart(cart)
        } returns mockk()

        val sut = CartService();
        runBlocking {
            var response = sut.addToCart("d54918c2aeda310f2d3a21c2f2570ac3", addtoCartRequest)
            assertNotNull(response)
        }

    }

    @DisplayName("it should return successful add to cart  when cart is null")
    @Test
    fun addToCartSuccessWhenDBCartNull() {
        val productOutlineElement: ProductOutlineElement = ProductOutlineElement(
            "229869",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            "https://media.croma.com/image/upload/v1605269918/Croma%20Assets/Communication/Mobiles/Images/8999510310942.png",
            84900.0,
            69900.0,
            18,
            "18%",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            true,
            0.0,
            1.0,
            "2022-07-26T17:35:25.233+05:30",
            true,
            ""
        )

        var products = listOf(productOutlineElement)
        var productOutline: ProductOutline = ProductOutline(products)

        var addtoCartRequest = CartRequest(
            "229869", 69900.0, 84900.0, 69900.0, "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5", 1, "302015", "electronics", productOutline
        )
        var cartProductDto = CartProductDto(
            "229869",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            "electronics",
            0,
            "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            84900.0,
            69900.0,
            69900.0,
            "302015",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            Date(),
            Date()
        )
        val cartProductDtoList = mutableListOf<CartProductDto>(cartProductDto)
        val cart = Cart("d54918c2aeda310f2d3a21c2f2570ac3", cartProductDtoList, Date(), Date())

        coEvery {
            cartRepository.findCartByCustomerHash("d54918c2aeda310f2d3a21c2f2570ac3")
        } returns null

        coEvery {
            cartRepository.saveCart(cart)
        } returns mockk()

        val sut = CartService();
        runBlocking {
            var response = sut.addToCart("d54918c2aeda310f2d3a21c2f2570ac3", addtoCartRequest)
            assertNotNull(response)
        }

    }

    @DisplayName("it should throw error not enough product available")
    @Test
    fun addToCartThrowsErrorNotEnoughProductAvailable() {
        val productOutlineElement: ProductOutlineElement = ProductOutlineElement(
            "229869",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            "https://media.croma.com/image/upload/v1605269918/Croma%20Assets/Communication/Mobiles/Images/8999510310942.png",
            84900.0,
            69900.0,
            18,
            "18%",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            true,
            0.0,
            1.0,
            "2022-07-26T17:35:25.233+05:30",
            true,
            ""
        )

        var products = listOf(productOutlineElement)
        var productOutline: ProductOutline = ProductOutline(products)

        var addtoCartRequest = CartRequest(
            "229869", 69900.0, 84900.0, 69900.0, "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5", 1, "302015", "electronics", productOutline
        )
        var cartProductDto = CartProductDto(
            "229869",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            "electronics",
            1,
            "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            84900.0,
            69900.0,
            69900.0,
            "302015",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            Date(),
            Date()
        )
        val cartProductDtoList = mutableListOf<CartProductDto>(cartProductDto)
        val cart = Cart("d54918c2aeda310f2d3a21c2f2570ac3", cartProductDtoList, Date(), Date())

        coEvery {
            cartRepository.findCartByCustomerHash("d54918c2aeda310f2d3a21c2f2570ac3")
        } returns cart

        coEvery {
            cartRepository.saveCart(cart)
        } returns mockk()

        val sut = CartService();
        runBlocking {
            val exception = assertFailsWith<HttpResponseException>(
                block = { sut.addToCart("d54918c2aeda310f2d3a21c2f2570ac3", addtoCartRequest) }
            )
            assertEquals("Not enough product available", exception.data)
        }

    }

    @DisplayName("it should throw error requested quantity is greater than max quantity")
    @Test
    fun addToCartThrowsErrorReqQtyGreaterThanMaxQty() {
        val productOutlineElement: ProductOutlineElement = ProductOutlineElement(
            "229869",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            "https://media.croma.com/image/upload/v1605269918/Croma%20Assets/Communication/Mobiles/Images/8999510310942.png",
            84900.0,
            69900.0,
            18,
            "18%",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            true,
            0.0,
            8.0,
            "2022-07-26T17:35:25.233+05:30",
            true,
            ""
        )

        var products = listOf(productOutlineElement)
        var productOutline: ProductOutline = ProductOutline(products)

        var addtoCartRequest = CartRequest(
            "229869", 69900.0, 84900.0, 69900.0, "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5", 2, "302015", "electronics", productOutline
        )
        var cartProductDto = CartProductDto(
            "229869",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            "electronics",
            4,
            "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            84900.0,
            69900.0,
            69900.0,
            "302015",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            Date(),
            Date()
        )
        val cartProductDtoList = mutableListOf<CartProductDto>(cartProductDto)
        val cart = Cart("d54918c2aeda310f2d3a21c2f2570ac3", cartProductDtoList, Date(), Date())

        coEvery {
            cartRepository.findCartByCustomerHash("d54918c2aeda310f2d3a21c2f2570ac3")
        } returns cart

        coEvery {
            cartRepository.saveCart(cart)
        } returns mockk()

        val sut = CartService();
        runBlocking {
            val exception = assertFailsWith<HttpResponseException>(
                block = { sut.addToCart("d54918c2aeda310f2d3a21c2f2570ac3", addtoCartRequest) }
            )
            assertEquals("Requested quantity is greater than max quantity", exception.data)
        }

    }

    @DisplayName("it should return successful reviewCart cart when cart not null")
    @Test
    fun reviewCartSuccessWhenDBCartNotNull() {
        val productOutlineElement: ProductOutlineElement = ProductOutlineElement(
            "229869",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            "https://media.croma.com/image/upload/v1605269918/Croma%20Assets/Communication/Mobiles/Images/8999510310942.png",
            84900.0,
            69900.0,
            18,
            "18%",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            true,
            0.0,
            1.0,
            "2022-07-26T17:35:25.233+05:30",
            true,
            ""
        )

        var products = listOf(productOutlineElement)
        var productOutline: ProductOutline = ProductOutline(products)

        var reviewCartRequest = ReviewCartRequest("electronics", "302015", productOutline)

        var cartProductDto = CartProductDto(
            "229869",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            "electronics",
            0,
            "tcpflsac520-bc7a-1a10-8675-dg6516f1f121:FlashComm335",
            84900.0,
            69900.0,
            69900.0,
            "302015",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            Date(),
            Date()
        )
        val cartProductDtoList = mutableListOf<CartProductDto>(cartProductDto)
        val cart = Cart("d54918c2aeda310f2d3a21c2f2570ac3", cartProductDtoList, Date(), Date())

        coEvery {
            cartRepository.findCartByCustomerHash("d54918c2aeda310f2d3a21c2f2570ac3")
        } returns cart

        coEvery {
            dataMapperUtils.mapReviewCartResponse(cart,reviewCartRequest)
        } returns mockk()

        coEvery {
            dataMapperUtils.mapCartResponseToCartSchema(any(),reviewCartRequest.pinCode)
        } returns mockk()

        coEvery {
            cartRepository.findCartAndUpdate("d54918c2aeda310f2d3a21c2f2570ac3",any())
        } returns mockk()

        val sut = CartService();
        runBlocking {
            var response = sut.reviewCart("d54918c2aeda310f2d3a21c2f2570ac3", reviewCartRequest)
            assertNotNull(response)
        }

    }

    @DisplayName("it should return successful reviewCart when cart null")
    @Test
    fun reviewCartSuccessWhenDBCartNull() {
        val productOutlineElement: ProductOutlineElement = ProductOutlineElement(
            "229869",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            "https://media.croma.com/image/upload/v1605269918/Croma%20Assets/Communication/Mobiles/Images/8999510310942.png",
            84900.0,
            69900.0,
            18,
            "18%",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            true,
            0.0,
            1.0,
            "2022-07-26T17:35:25.233+05:30",
            true,
            ""
        )

        var products = listOf(productOutlineElement)
        var productOutline: ProductOutline = ProductOutline(products)

        var reviewCartRequest = ReviewCartRequest("electronics", "302015", productOutline)


        coEvery {
            cartRepository.findCartByCustomerHash("d54918c2aeda310f2d3a21c2f2570ac3")
        } returns null

        coEvery {
            dataMapperUtils.mapCartEmptyResponse("d54918c2aeda310f2d3a21c2f2570ac3")
        } returns mockk()

        val sut = CartService();
        runBlocking {
            var response = sut.reviewCart("d54918c2aeda310f2d3a21c2f2570ac3", reviewCartRequest)
            assertNotNull(response)
        }

    }

    @DisplayName("it should return successful reviewCart  when cart  items zero")
    @Test
    fun reviewCartSuccessWhenCartItemsZero() {
        val productOutlineElement: HotelOutlineElement = HotelOutlineElement(
            "229869",
            "Apple iPhone 12 (64GB ROM, 4GB RAM, MGJ53HN/A, Black",
            "https://media.croma.com/image/upload/v1605269918/Croma%20Assets/Communication/Mobiles/Images/8999510310942.png",
            84900.0,
            69900.0,
            18,
            "18%",
            "73eb6345-9cc9-4c37-a8e8-8620d6d32cf5",
            true,
            0.0,
            1.0,
            "2022-07-26T17:35:25.233+05:30",
            true,
            ""
        )

        var products = listOf(productOutlineElement)
        var productOutline: ProductOutline = ProductOutline(products)

        var reviewCartRequest = ReviewCartRequest("electronics", "302015", productOutline)

        val cartProductDtoList = mutableListOf<CartProductDto>()
        val cart = Cart("d54918c2aeda310f2d3a21c2f2570ac3", cartProductDtoList, Date(), Date())

        coEvery {
            cartRepository.findCartByCustomerHash("d54918c2aeda310f2d3a21c2f2570ac3")
        } returns cart

        coEvery {
            dataMapperUtils.mapCartEmptyResponse("d54918c2aeda310f2d3a21c2f2570ac3")
        } returns mockk()

        val sut = CartService();
        runBlocking {
            var response = sut.reviewCart("d54918c2aeda310f2d3a21c2f2570ac3", reviewCartRequest)
            assertNotNull(response)
        }

    }
*/

}