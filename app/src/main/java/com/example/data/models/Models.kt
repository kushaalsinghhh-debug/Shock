package com.example.data.models

import java.util.UUID

enum class UserRole {
    USER, STAFF, ADMIN
}

enum class TournamentStatus {
    UPCOMING, LIVE, COMPLETED, CANCELLED
}

enum class KYCStatus {
    NOT_SUBMITTED, PENDING, VERIFIED, REJECTED
}

enum class TransactionType {
    DEPOSIT, WITHDRAW, JOIN_TOURNAMENT, WINNING_REWARD, BONUS_CREDIT, REFUND
}

enum class TransactionStatus {
    PENDING, SUCCESS, REJECTED, CANCELLED
}

enum class DepositStatus {
    PENDING, APPROVED, REJECTED
}

data class DepositRequest(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userName: String,
    val userPhone: String = "",
    val amount: Double,
    val paymentMode: String = "MANUAL_UPI", // "MANUAL_UPI" or "GATEWAY"
    val upiId: String = "",
    val txnId: String = "", // UTR / Reference ID submitted by user
    val status: DepositStatus = DepositStatus.PENDING,
    val rejectReason: String = "",
    val timestamp: String = "Just now"
)

data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Pro Gamer",
    val phone: String = "+91 9876543210",
    val email: String = "player@winx7.gg",
    val ffUid: String = "7654321098",
    val role: UserRole = UserRole.USER,
    val isBanned: Boolean = false,
    val isSuspended: Boolean = false,
    val kycStatus: KYCStatus = KYCStatus.VERIFIED,
    val depositBalance: Double = 250.0,
    val winningBalance: Double = 500.0,
    val bonusBalance: Double = 50.0,
    val referralCode: String = "WINX7VIP",
    val referredBy: String? = null,
    val avatarUrl: String = "",
    val totalMatchesJoined: Int = 14,
    val totalKills: Int = 42,
    val totalWins: Int = 6
) {
    val totalBalance: Double get() = depositBalance + winningBalance + bonusBalance
}

data class Category(
    val id: String,
    val name: String,
    val iconName: String = "sports_esports",
    val isEnabled: Boolean = true
)

data class Tournament(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val categoryId: String,
    val categoryName: String,
    val bannerUrl: String = "",
    val thumbnailUrl: String = "",
    val mapName: String = "Bermuda",
    val mode: String = "Solo", // Solo, Duo, Squad
    val totalSlots: Int = 48,
    val joinedSlots: Int = 24,
    val prizePool: Double = 1000.0,
    val entryFee: Double = 25.0,
    val killReward: Double = 10.0,
    val matchTime: String = "08:00 PM",
    val matchDate: String = "26 July",
    val status: TournamentStatus = TournamentStatus.UPCOMING,
    val isFree: Boolean = false,
    val isFeatured: Boolean = false,
    val isRecommended: Boolean = true,
    val isPrivate: Boolean = false,
    val roomId: String = "",
    val roomPassword: String = "",
    val rules: String = "1. No emulators allowed.\n2. Hacks/Cheats lead to permanent ban.\n3. Room ID & Password shared 15 mins before match.",
    val description: String = "Compete with top Free Fire players and win cash rewards instantly into your wallet!",
    val participants: List<String> = emptyList(), // list of FF UIDs or player names
    val winnerNote: String = ""
)

data class WalletTransaction(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val amount: Double,
    val type: TransactionType,
    val status: TransactionStatus = TransactionStatus.SUCCESS,
    val title: String,
    val description: String = "",
    val timestamp: String = "Just now",
    val referenceId: String = "TXN-${System.currentTimeMillis() % 1000000}"
)

data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val ffUid: String,
    val kills: Int,
    val wins: Int,
    val totalEarnings: Double,
    val avatarUrl: String = ""
)

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: String = "5m ago",
    val type: String = "GENERAL", // GENERAL, TOURNAMENT, WALLET, OFFER
    val isRead: Boolean = false
)

data class SupportTicket(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userName: String,
    val subject: String,
    val message: String,
    val reply: String = "",
    val status: String = "OPEN", // OPEN, RESOLVED
    val timestamp: String = "Today"
)

data class Coupon(
    val id: String = UUID.randomUUID().toString(),
    val code: String,
    val bonusAmount: Double,
    val minDeposit: Double = 0.0,
    val isExpired: Boolean = false,
    val usageLimit: Int = 100,
    val currentUsage: Int = 12
)

data class Announcement(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val isScrolling: Boolean = true,
    val isPopup: Boolean = false
)

data class BannerItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val imageUrl: String,
    val actionUrl: String = ""
)

data class AdminConfig(
    val isMaintenanceMode: Boolean = false,
    val isRegistrationOn: Boolean = true,
    val isTournamentOn: Boolean = true,
    val isWithdrawOn: Boolean = true,
    val isDepositOn: Boolean = true,
    val isReferralOn: Boolean = true,
    val minAppVersion: String = "1.0.0",
    val whatsappContact: String = "+91 9999988888",
    val telegramContact: String = "https://t.me/winx7_official",
    val referralRewardAmount: Double = 25.0,
    // Deposit Settings (Manual vs Gateway)
    val depositMode: String = "MANUAL", // "MANUAL" or "GATEWAY"
    val manualUpiId: String = "winx7pay@upi",
    val manualQrUrl: String = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=upi://pay?pa=winx7pay@upi&pn=WinX7Esports&am=100",
    val manualQrInstruction: String = "Scan QR code using GPay/PhonePe/Paytm. Enter amount and paste your 12-digit UTR/Txn ID for instant verification.",
    val gatewayProvider: String = "Cashfree", // Razorpay, Cashfree, Paytm, PhonePe
    val gatewayMerchantId: String = "MCH_WINX7_98213",
    val gatewayApiKey: String = "key_live_winx7_9981245821",
    val gatewaySecretKey: String = "secret_live_winx7_881239"
)

data class StaffUser(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val roleTitle: String = "Match Moderator",
    val permissions: List<String> = listOf("UPLOAD_ROOM", "UPDATE_RESULTS", "SUPPORT_TICKETS")
)
