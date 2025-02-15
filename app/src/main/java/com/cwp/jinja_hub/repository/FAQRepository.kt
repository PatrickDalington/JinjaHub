package com.cwp.jinja_hub.repository

import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.CardItem
import com.cwp.jinja_hub.model.FAQCardItem
import com.cwp.jinja_hub.model.ServicesCategory
import kotlinx.coroutines.delay

class FAQRepository {

    // Simulate fetching categories
    suspend fun fetchFAQ(): List<FAQCardItem> {
        delay(1000) // Simulate network delay
        val additionalFAQItems = listOf(
            FAQCardItem(
                "5",
                "How do I place an order for Jinja?",
                "You can place your order through our website or mobile app. Simply select your desired products and follow the checkout process."
            ),
            FAQCardItem(
                "6",
                "What payment methods are accepted?",
                "We accept various payment methods including credit/debit cards, mobile money, and bank transfers to ensure convenience for our customers."
            ),
            FAQCardItem(
                "7",
                "Is there a refund policy for Jinja?",
                "Yes, we have a refund policy in place. If you are unsatisfied with your purchase, please contact our support team within 30 days for assistance."
            ),
            FAQCardItem(
                "8",
                "How long does shipping take?",
                "Shipping times vary depending on your location. Typically, orders are delivered within 3-5 business days."
            ),
            FAQCardItem(
                "9",
                "Can I track my order after purchase?",
                "Absolutely! Once your order is confirmed, you will receive a tracking number via email or SMS to monitor your delivery status."
            ),
            FAQCardItem(
                "10",
                "Does Jinja offer any discounts or promotions?",
                "Yes, we regularly offer discounts and promotions. Keep an eye on our website or subscribe to our newsletter to stay updated."
            ),
            FAQCardItem(
                "11",
                "How do I contact customer support?",
                "You can reach our customer support team via email, phone, or the live chat option available on our website for any inquiries or assistance."
            ),
            FAQCardItem(
                "12",
                "Is Jinja available in all regions?",
                "Jinja is available in many regions. However, availability may vary depending on your location. Please check our website for the latest information."
            ),
            FAQCardItem(
                "13",
                "What should I do if I receive a damaged product?",
                "If you receive a damaged product, please contact our customer support immediately with your order details. We will work with you to resolve the issue promptly."
            ),
            FAQCardItem(
                "14",
                "How can I stay updated on new products and offers?",
                "You can stay informed by following us on social media, subscribing to our newsletter, or regularly visiting our website for updates on new products and special offers."
            )
        )

        return additionalFAQItems
    }
}
