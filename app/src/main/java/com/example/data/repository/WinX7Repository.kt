package com.example.data.repository

import android.util.Log
import com.example.data.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object WinX7Repository {

    private const val TAG = "WinX7Repository"
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Firebase instances are mandatory for authentication and profile storage.
    private val firebaseAuth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Throwable) {
        Log.w(TAG, "FirebaseAuth unavailable: ${e.message}")
        null
    }

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Throwable) {
        Log.w(TAG, "Firestore unavailable: ${e.message}")
        null
    }

    private val _authReady = MutableStateFlow(false)
    val authReady: StateFlow<Boolean> = _authReady.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        firebaseAuth?.let { auth ->
            val initialUser = auth.currentUser
            _isAuthenticated.value = initialUser != null
            _authReady.value = true
            auth.addAuthStateListener { updatedAuth ->
                val fu = updatedAuth.currentUser
                _isAuthenticated.value = fu != null
                _authReady.value = true
                if (fu != null) {
                    loadUserProfileFromRemote(fu)
                } else {
                    _currentPortalRole.value = UserRole.USER
                }
            }
        }
    }

    // Active Current User
    private val _currentUser = MutableStateFlow(UserProfile())
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Current Portal Role (USER, STAFF, ADMIN)
    private val _currentPortalRole = MutableStateFlow(UserRole.USER)
    val currentPortalRole: StateFlow<UserRole> = _currentPortalRole.asStateFlow()

    // Categories
    private val defaultCategories = listOf(
        Category("cat-1", "Solo BR", "person"),
        Category("cat-2", "Duo BR", "group"),
        Category("cat-3", "Squad BR", "groups"),
        Category("cat-4", "CS Solo", "flash_on"),
        Category("cat-5", "CS Duo", "people_flex"),
        Category("cat-6", "CS Squad", "shield"),
        Category("cat-7", "Lone Wolf", "sports_kabaddi"),
        Category("cat-8", "1 vs 1", "swords"),
        Category("cat-9", "2 vs 2", "groups_2"),
        Category("cat-10", "Elite Solo", "workspace_premium"),
        Category("cat-11", "Unlimited Ammo", "all_inclusive"),
        Category("cat-12", "Bomb Squad", "timer"),
        Category("cat-13", "Custom Tournament", "tune"),
        Category("cat-14", "Private Tournament", "lock")
    )
    private val _categories = MutableStateFlow(defaultCategories)
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    // Tournaments
    private val defaultTournaments = mutableListOf(
        Tournament(
            id = "tour-101",
            title = "#101 FREE FIRE MEGA SOLO BERMUDA",
            categoryId = "cat-1",
            categoryName = "Solo BR",
            mapName = "Bermuda",
            mode = "Solo",
            totalSlots = 48,
            joinedSlots = 38,
            prizePool = 1500.0,
            entryFee = 30.0,
            killReward = 15.0,
            matchTime = "09:00 PM",
            matchDate = "26 July",
            status = TournamentStatus.UPCOMING,
            isFree = false,
            isFeatured = true,
            isRecommended = true,
            roomId = "8899201",
            roomPassword = "777",
            description = "High intensity Solo Battle Royale on Bermuda map. Top 3 get extra trophy bonuses!",
            participants = listOf("ShadowX Gamer", "ViperFF", "ProSniper99", "AWM_King", "DemonRider", "FreeFireGod")
        ),
        Tournament(
            id = "tour-102",
            title = "#102 CS SQUAD UNLIMITED AMMO SHOWDOWN",
            categoryId = "cat-6",
            categoryName = "CS Squad",
            mapName = "Kalahari",
            mode = "Squad",
            totalSlots = 8,
            joinedSlots = 8,
            prizePool = 2000.0,
            entryFee = 0.0,
            killReward = 20.0,
            matchTime = "07:30 PM",
            matchDate = "26 July",
            status = TournamentStatus.LIVE,
            isFree = true,
            isFeatured = true,
            roomId = "443321",
            roomPassword = "123",
            description = "FREE ENTRY Clash Squad Showdown with Unlimited Ammo! Watch live or play!",
            participants = listOf("ShadowX Gamer", "TeamAlpha1", "TeamAlpha2", "RogueSquad", "GhostRiders")
        ),
        Tournament(
            id = "tour-103",
            title = "#103 DUO PURGATORY NIGHT HUNTERS",
            categoryId = "cat-2",
            categoryName = "Duo BR",
            mapName = "Purgatory",
            mode = "Duo",
            totalSlots = 24,
            joinedSlots = 14,
            prizePool = 1200.0,
            entryFee = 20.0,
            killReward = 10.0,
            matchTime = "10:00 PM",
            matchDate = "27 July",
            status = TournamentStatus.UPCOMING,
            isFree = false,
            isRecommended = true,
            description = "Duo survival in Purgatory. Bring your partner and conquer the island!"
        ),
        Tournament(
            id = "tour-104",
            title = "#104 1 vs 1 LONE WOLF BATTLE ROYALE",
            categoryId = "cat-8",
            categoryName = "1 vs 1",
            mapName = "Iron Cage",
            mode = "1 vs 1",
            totalSlots = 2,
            joinedSlots = 1,
            prizePool = 300.0,
            entryFee = 50.0,
            killReward = 50.0,
            matchTime = "11:15 PM",
            matchDate = "27 July",
            status = TournamentStatus.UPCOMING,
            isFree = false,
            description = "Direct 1v1 duel. Winner takes all!"
        ),
        Tournament(
            id = "tour-105",
            title = "#100 ELITE SOLO CHAMPIONSHIP",
            categoryId = "cat-10",
            categoryName = "Elite Solo",
            mapName = "Alpine",
            mode = "Solo",
            totalSlots = 48,
            joinedSlots = 48,
            prizePool = 5000.0,
            entryFee = 100.0,
            killReward = 25.0,
            matchTime = "04:00 PM",
            matchDate = "Yesterday",
            status = TournamentStatus.COMPLETED,
            roomId = "112233",
            roomPassword = "999",
            winnerNote = "1st: ShadowX Gamer (12 Kills) - ₹1200\n2nd: ViperFF (8 Kills) - ₹600\n3rd: DemonRider (5 Kills) - ₹300"
        )
    )
    private val _tournaments = MutableStateFlow<List<Tournament>>(defaultTournaments)
    val tournaments: StateFlow<List<Tournament>> = _tournaments.asStateFlow()

    // Joined Tournament IDs for current user
    private val _joinedTournamentIds = MutableStateFlow<Set<String>>(setOf("tour-101", "tour-102", "tour-105"))
    val joinedTournamentIds: StateFlow<Set<String>> = _joinedTournamentIds.asStateFlow()

    // Wallet Transactions
    private val defaultTransactions = listOf(
        WalletTransaction(
            userId = "usr-1001",
            amount = 300.0,
            type = TransactionType.DEPOSIT,
            status = TransactionStatus.SUCCESS,
            title = "Deposit via UPI",
            timestamp = "2 hours ago"
        ),
        WalletTransaction(
            userId = "usr-1001",
            amount = 30.0,
            type = TransactionType.JOIN_TOURNAMENT,
            status = TransactionStatus.SUCCESS,
            title = "Joined Tournament #101",
            timestamp = "3 hours ago"
        ),
        WalletTransaction(
            userId = "usr-1001",
            amount = 450.0,
            type = TransactionType.WINNING_REWARD,
            status = TransactionStatus.SUCCESS,
            title = "Won Tournament #100 Prize",
            timestamp = "Yesterday"
        )
    )
    private val _transactions = MutableStateFlow(defaultTransactions)
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    // Leaderboard
    private val defaultLeaderboard = listOf(
        LeaderboardEntry(1, "ViperFF_God", "88231044", 142, 28, 12500.0),
        LeaderboardEntry(2, "ShadowX Gamer", "7654321098", 128, 24, 10800.0),
        LeaderboardEntry(3, "DemonSniper", "99410211", 115, 19, 8900.0),
        LeaderboardEntry(4, "FreeFireKing", "11204910", 98, 15, 6400.0),
        LeaderboardEntry(5, "RogueOne", "66501928", 84, 12, 5200.0),
        LeaderboardEntry(6, "AWM_Master", "33219800", 79, 10, 4300.0),
        LeaderboardEntry(7, "NinjaLegend", "44102948", 68, 8, 3800.0)
    )
    private val _leaderboard = MutableStateFlow(defaultLeaderboard)
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    // Notifications
    private val defaultNotifications = listOf(
        AppNotification(title = "Room ID Published!", message = "Room ID and Password for #101 Bermuda Solo is now available in My Matches.", timestamp = "10m ago", type = "TOURNAMENT"),
        AppNotification(title = "Deposit Successful", message = "₹300 credited to your deposit wallet via UPI.", timestamp = "2h ago", type = "WALLET"),
        AppNotification(title = "Mega Tournament Promo", message = "Use coupon 'WINX7BONUS' for 20% bonus on next deposit!", timestamp = "1d ago", type = "OFFER")
    )
    private val _notifications = MutableStateFlow(defaultNotifications)
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Support Tickets
    private val defaultTickets = listOf(
        SupportTicket(
            id = "tkt-501",
            userId = "usr-1001",
            userName = "ShadowX Gamer",
            subject = "Room ID delayed for Match #101",
            message = "When will room password be updated?",
            reply = "Room ID & Password will be published 15 minutes before match start time.",
            status = "RESOLVED",
            timestamp = "Today"
        )
    )
    private val _tickets = MutableStateFlow(defaultTickets)
    val tickets: StateFlow<List<SupportTicket>> = _tickets.asStateFlow()

    // Coupons
    private val defaultCoupons = listOf(
        Coupon(code = "WELCOME100", bonusAmount = 50.0, minDeposit = 100.0),
        Coupon(code = "WINX7VIP", bonusAmount = 100.0, minDeposit = 200.0),
        Coupon(code = "FREEFIRE50", bonusAmount = 25.0, minDeposit = 50.0)
    )
    private val _coupons = MutableStateFlow(defaultCoupons)
    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()

    // Banners & Announcements
    private val defaultBanners = listOf(
        BannerItem("b-1", "FREE FIRE SEASON 10 GRAND TOURNAMENT", ""),
        BannerItem("b-2", "INSTANT WITHDRAWAL TO UPI & PAYTM 24/7", ""),
        BannerItem("b-3", "REFER FRIENDS AND EARN ₹25 BONUS PER REFERRAL", "")
    )
    private val _banners = MutableStateFlow(defaultBanners)
    val banners: StateFlow<List<BannerItem>> = _banners.asStateFlow()

    private val _announcements = MutableStateFlow(
        listOf(
            Announcement("ann-1", "NOTICE", "🔥 WinX7 Free Fire Daily Tournaments are live! Join now & win instant cash into your wallet. Room ID shared 15m before match!")
        )
    )
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    // Config
    private val _adminConfig = MutableStateFlow(AdminConfig())
    val adminConfig: StateFlow<AdminConfig> = _adminConfig.asStateFlow()

    // Deposit Requests (Manual & Gateway)
    private val defaultDepositRequests = listOf(
        DepositRequest(
            id = "DEP-1001",
            userId = "usr-1002",
            userName = "ViperFF_God",
            userPhone = "+91 9123456789",
            amount = 500.0,
            paymentMode = "MANUAL_UPI",
            upiId = "viper@paytm",
            txnId = "420188921034",
            status = DepositStatus.PENDING,
            timestamp = "10m ago"
        ),
        DepositRequest(
            id = "DEP-1002",
            userId = "usr-1003",
            userName = "DemonSniper",
            userPhone = "+91 9988776655",
            amount = 200.0,
            paymentMode = "MANUAL_UPI",
            upiId = "demon@ybl",
            txnId = "420199381204",
            status = DepositStatus.APPROVED,
            timestamp = "1h ago"
        )
    )
    private val _depositRequests = MutableStateFlow(defaultDepositRequests)
    val depositRequests: StateFlow<List<DepositRequest>> = _depositRequests.asStateFlow()

    // Staff Users (Admin managed)
    private val defaultStaff = listOf(
        StaffUser("stf-1", "Rohan Moderator", "rohan@winx7.gg", "Match Moderator"),
        StaffUser("stf-2", "Ankit AdminAssist", "ankit@winx7.gg", "Result Inspector")
    )
    private val _staffUsers = MutableStateFlow(defaultStaff)
    val staffUsers: StateFlow<List<StaffUser>> = _staffUsers.asStateFlow()

    // All Users list for Admin
    private val defaultUserList = listOf(
        UserProfile("usr-admin-1", "Kushaal Singh (Super Admin)", "9294667072", "kushaal.singhhh@gmail.com", "9900112233", UserRole.ADMIN, depositBalance = 10000.0, winningBalance = 25000.0),
        UserProfile("usr-1001", "ShadowX Gamer", "+91 9876543210", "player@winx7.gg", "7654321098", UserRole.USER, depositBalance = 250.0, winningBalance = 450.0),
        UserProfile("usr-1002", "ViperFF_God", "+91 9123456789", "viper@winx7.gg", "88231044", UserRole.USER, depositBalance = 500.0, winningBalance = 2400.0),
        UserProfile("usr-1003", "DemonSniper", "+91 9988776655", "demon@winx7.gg", "99410211", UserRole.USER, depositBalance = 100.0, winningBalance = 150.0),
        UserProfile("usr-1004", "RogueGamer", "+91 9811223344", "rogue@winx7.gg", "66501928", UserRole.USER, isSuspended = true)
    )
    private val _allUsers = MutableStateFlow(defaultUserList)
    val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()

    // --- Actions ---

    fun switchPortalRole(role: UserRole) {
        _currentPortalRole.value = role
    }

    fun joinTournament(tournamentId: String): Pair<Boolean, String> {
        val user = _currentUser.value
        val tour = _tournaments.value.find { it.id == tournamentId } ?: return Pair(false, "Tournament not found")

        if (_joinedTournamentIds.value.contains(tournamentId)) {
            return Pair(false, "You have already joined this tournament!")
        }

        if (tour.joinedSlots >= tour.totalSlots) {
            return Pair(false, "Tournament slots are full!")
        }

        if (user.totalBalance < tour.entryFee) {
            return Pair(false, "Insufficient balance! Please add deposit money.")
        }

        // Deduct entry fee: First from bonus (max 10%), then deposit, then winning
        var fee = tour.entryFee
        var newBonus = user.bonusBalance
        var newDeposit = user.depositBalance
        var newWinning = user.winningBalance

        if (newBonus > 0 && fee > 0) {
            val bonusDeduct = minOf(newBonus, fee * 0.1) // 10% bonus usage limit
            newBonus -= bonusDeduct
            fee -= bonusDeduct
        }

        if (newDeposit > 0 && fee > 0) {
            val depDeduct = minOf(newDeposit, fee)
            newDeposit -= depDeduct
            fee -= depDeduct
        }

        if (fee > 0) {
            newWinning -= fee
        }

        _currentUser.value = user.copy(
            depositBalance = newDeposit,
            bonusBalance = newBonus,
            winningBalance = newWinning,
            totalMatchesJoined = user.totalMatchesJoined + 1
        )

        // Update tournament slots
        _tournaments.value = _tournaments.value.map {
            if (it.id == tournamentId) {
                it.copy(
                    joinedSlots = it.joinedSlots + 1,
                    participants = it.participants + user.name
                )
            } else it
        }

        _joinedTournamentIds.value = _joinedTournamentIds.value + tournamentId

        // Add Transaction log
        val txn = WalletTransaction(
            userId = user.id,
            amount = tour.entryFee,
            type = TransactionType.JOIN_TOURNAMENT,
            status = TransactionStatus.SUCCESS,
            title = "Joined ${tour.title}"
        )
        _transactions.value = listOf(txn) + _transactions.value

        return Pair(true, "Successfully joined tournament seat reserved!")
    }

    fun addDeposit(amount: Double): Boolean {
        val user = _currentUser.value
        _currentUser.value = user.copy(depositBalance = user.depositBalance + amount)
        val txn = WalletTransaction(
            userId = user.id,
            amount = amount,
            type = TransactionType.DEPOSIT,
            status = TransactionStatus.SUCCESS,
            title = "Instant Deposit"
        )
        _transactions.value = listOf(txn) + _transactions.value
        return true
    }

    fun submitDepositRequest(
        amount: Double,
        txnId: String,
        upiId: String = "",
        paymentMode: String = "MANUAL_UPI"
    ): Pair<Boolean, String> {
        val user = _currentUser.value

        if (amount <= 0) return Pair(false, "Deposit amount must be greater than ₹0")

        if (paymentMode == "GATEWAY") {
            // Gateway Instant Deposit
            _currentUser.value = user.copy(depositBalance = user.depositBalance + amount)
            val txn = WalletTransaction(
                userId = user.id,
                amount = amount,
                type = TransactionType.DEPOSIT,
                status = TransactionStatus.SUCCESS,
                title = "Payment Gateway Deposit",
                referenceId = txnId.ifBlank { "GW-${System.currentTimeMillis() % 1000000}" }
            )
            _transactions.value = listOf(txn) + _transactions.value

            val req = DepositRequest(
                userId = user.id,
                userName = user.name,
                userPhone = user.phone,
                amount = amount,
                paymentMode = "GATEWAY",
                upiId = upiId,
                txnId = txn.referenceId,
                status = DepositStatus.APPROVED,
                timestamp = "Just now"
            )
            _depositRequests.value = listOf(req) + _depositRequests.value
            return Pair(true, "₹${amount.toInt()} deposited instantly via Payment Gateway API!")
        } else {
            // Manual UPI QR Deposit Verification Request
            if (txnId.isBlank() || txnId.length < 6) {
                return Pair(false, "Please enter a valid 12-digit UPI UTR / Transaction ID!")
            }

            // Check duplicate txnId
            if (_depositRequests.value.any { it.txnId.equals(txnId, ignoreCase = true) && it.status != DepositStatus.REJECTED }) {
                return Pair(false, "This Transaction ID / UTR has already been submitted for verification!")
            }

            val req = DepositRequest(
                userId = user.id,
                userName = user.name,
                userPhone = user.phone,
                amount = amount,
                paymentMode = "MANUAL_UPI",
                upiId = upiId.ifBlank { user.phone },
                txnId = txnId,
                status = DepositStatus.PENDING,
                timestamp = "Just now"
            )
            _depositRequests.value = listOf(req) + _depositRequests.value

            val txn = WalletTransaction(
                userId = user.id,
                amount = amount,
                type = TransactionType.DEPOSIT,
                status = TransactionStatus.PENDING,
                title = "Manual UPI Deposit (Pending Verification)",
                description = "UTR: $txnId",
                referenceId = txnId
            )
            _transactions.value = listOf(txn) + _transactions.value

            val notif = AppNotification(
                title = "Deposit Request Submitted",
                message = "Your manual UPI deposit of ₹${amount.toInt()} (UTR: $txnId) is submitted and under verification.",
                type = "WALLET"
            )
            _notifications.value = listOf(notif) + _notifications.value

            return Pair(true, "Deposit request submitted successfully! Transaction ID $txnId is pending verification by admin.")
        }
    }

    fun approveDepositRequest(requestId: String): Pair<Boolean, String> {
        val request = _depositRequests.value.find { it.id == requestId }
            ?: return Pair(false, "Deposit request not found!")

        if (request.status != DepositStatus.PENDING) {
            return Pair(false, "Request is already ${request.status.name}!")
        }

        // Update request status
        _depositRequests.value = _depositRequests.value.map {
            if (it.id == requestId) it.copy(status = DepositStatus.APPROVED) else it
        }

        // Credit user deposit balance
        if (request.userId == _currentUser.value.id) {
            _currentUser.value = _currentUser.value.copy(
                depositBalance = _currentUser.value.depositBalance + request.amount
            )
        }
        _allUsers.value = _allUsers.value.map { usr ->
            if (usr.id == request.userId) usr.copy(depositBalance = usr.depositBalance + request.amount) else usr
        }

        // Update wallet transaction status
        _transactions.value = _transactions.value.map { txn ->
            if (txn.referenceId == request.txnId || (txn.userId == request.userId && txn.amount == request.amount && txn.status == TransactionStatus.PENDING)) {
                txn.copy(status = TransactionStatus.SUCCESS, title = "Manual Deposit Approved")
            } else txn
        }

        // Add user notification
        val notif = AppNotification(
            title = "Deposit Approved! 🎉",
            message = "₹${request.amount.toInt()} has been verified and credited to your deposit wallet.",
            type = "WALLET"
        )
        _notifications.value = listOf(notif) + _notifications.value

        return Pair(true, "Deposit request #${request.id} APPROVED! ₹${request.amount.toInt()} credited to ${request.userName}.")
    }

    fun rejectDepositRequest(requestId: String, reason: String = "Transaction ID / UTR verification failed"): Pair<Boolean, String> {
        val request = _depositRequests.value.find { it.id == requestId }
            ?: return Pair(false, "Deposit request not found!")

        if (request.status != DepositStatus.PENDING) {
            return Pair(false, "Request is already ${request.status.name}!")
        }

        // Update request status
        _depositRequests.value = _depositRequests.value.map {
            if (it.id == requestId) it.copy(status = DepositStatus.REJECTED, rejectReason = reason) else it
        }

        // Update transaction status
        _transactions.value = _transactions.value.map { txn ->
            if (txn.referenceId == request.txnId) {
                txn.copy(status = TransactionStatus.REJECTED, description = "Rejected: $reason")
            } else txn
        }

        // Add user notification
        val notif = AppNotification(
            title = "Deposit Request Rejected",
            message = "Your deposit of ₹${request.amount.toInt()} (UTR: ${request.txnId}) was rejected. Reason: $reason",
            type = "WALLET"
        )
        _notifications.value = listOf(notif) + _notifications.value

        return Pair(true, "Deposit request #${request.id} REJECTED.")
    }

    fun requestWithdraw(amount: Double): Pair<Boolean, String> {
        val user = _currentUser.value
        if (amount > user.winningBalance) {
            return Pair(false, "Withdrawal amount cannot exceed Winning Balance!")
        }
        if (amount < 50.0) {
            return Pair(false, "Minimum withdrawal is ₹50")
        }

        _currentUser.value = user.copy(winningBalance = user.winningBalance - amount)
        val txn = WalletTransaction(
            userId = user.id,
            amount = amount,
            type = TransactionType.WITHDRAW,
            status = TransactionStatus.PENDING,
            title = "Withdrawal Request to UPI"
        )
        _transactions.value = listOf(txn) + _transactions.value
        return Pair(true, "Withdrawal request submitted successfully!")
    }

    fun applyCoupon(code: String): Pair<Boolean, String> {
        val coupon = _coupons.value.find { it.code.equals(code, ignoreCase = true) }
            ?: return Pair(false, "Invalid coupon code!")

        if (coupon.isExpired) return Pair(false, "This coupon code has expired!")

        val user = _currentUser.value
        _currentUser.value = user.copy(bonusBalance = user.bonusBalance + coupon.bonusAmount)

        val txn = WalletTransaction(
            userId = user.id,
            amount = coupon.bonusAmount,
            type = TransactionType.BONUS_CREDIT,
            status = TransactionStatus.SUCCESS,
            title = "Coupon Bonus (${coupon.code})"
        )
        _transactions.value = listOf(txn) + _transactions.value
        return Pair(true, "₹${coupon.bonusAmount.toInt()} bonus added to your wallet!")
    }

    fun submitSupportTicket(subject: String, message: String) {
        val user = _currentUser.value
        val ticket = SupportTicket(
            userId = user.id,
            userName = user.name,
            subject = subject,
            message = message
        )
        _tickets.value = listOf(ticket) + _tickets.value
    }

    fun updateProfile(name: String, avatarUrl: String = "", email: String = "", phone: String = "") {
        _currentUser.value = _currentUser.value.copy(
            name = name.ifBlank { _currentUser.value.name },
            avatarUrl = avatarUrl.ifBlank { _currentUser.value.avatarUrl },
            email = if (email.isNotBlank()) email else _currentUser.value.email,
            phone = if (phone.isNotBlank()) phone else _currentUser.value.phone
        )
        // Persist to Firestore when available
        try {
            val uid = firebaseAuth?.currentUser?.uid
            if (uid != null && firestore != null) {
                val map = mapOf(
                    "id" to _currentUser.value.id,
                    "name" to _currentUser.value.name,
                    "email" to _currentUser.value.email,
                    "phone" to _currentUser.value.phone,
                    "avatarUrl" to _currentUser.value.avatarUrl,
                    "role" to _currentUser.value.role.name
                )
                firestore.collection("users").document(uid).set(map)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to persist profile: ${e.message}")
        }
    }

    fun uploadProfileImage(uri: android.net.Uri, onResult: (Boolean, String) -> Unit) {
        val uid = firebaseAuth?.currentUser?.uid
        if (uid == null) {
            onResult(false, "User not logged in.")
            return
        }
        try {
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference.child("avatars/$uid.jpg")
            storageRef.putFile(uri).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        updateProfile(name = _currentUser.value.name, avatarUrl = downloadUri.toString())
                        onResult(true, "Profile picture updated successfully.")
                    }.addOnFailureListener { e ->
                        onResult(false, e.localizedMessage ?: "Failed to get image URL.")
                    }
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Upload failed.")
                }
            }
        } catch (e: Throwable) {
            onResult(false, "Storage not initialized or error occurred.")
        }
    }

    // --- Firebase-backed Authentication & Profile management ---

    private fun loadUserProfileFromRemote(fu: FirebaseUser) {
        try {
            val docRef = firestore?.collection("users")?.document(fu.uid)
            if (docRef == null) {
                // No firestore configured; keep current local user
                return
            }
            docRef.get().addOnSuccessListener { snap ->
                if (snap != null && snap.exists()) {
                    try {
                        val remote = snap.toObject(UserProfile::class.java)
                        if (remote != null) {
                            _currentUser.value = remote
                        } else {
                            // If deserialization failed, create a basic profile
                            _currentUser.value = _currentUser.value.copy(
                                id = fu.uid,
                                name = fu.displayName ?: _currentUser.value.name,
                                email = fu.email ?: _currentUser.value.email
                            )
                        }
                    } catch (e: Throwable) {
                        Log.w(TAG, "Error parsing remote profile: ${e.message}")
                    }
                } else {
                    // Create a minimal profile document for this user
                    val profile = UserProfile(
                        id = fu.uid,
                        name = fu.displayName ?: _currentUser.value.name,
                        phone = fu.phoneNumber ?: _currentUser.value.phone,
                        email = fu.email ?: _currentUser.value.email,
                        ffUid = fu.uid,
                        role = UserRole.USER
                    )
                    _currentUser.value = profile
                    firestore.collection("users").document(fu.uid).set(profile)
                }
            }.addOnFailureListener { ex ->
                Log.w(TAG, "Failed to load profile: ${ex.message}")
            }
        } catch (e: Throwable) {
            Log.w(TAG, "loadUserProfileFromRemote failed: ${e.message}")
        }
    }

    fun signIn(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        val auth = firebaseAuth
        if (auth == null) {
            onResult(false, "Firebase is not available on this device.")
            return
        }

        val trimmedEmail = email.trim()
        val trimmedPassword = password
        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            onResult(false, "Please enter both email and password.")
            return
        }
        if (!isValidEmail(trimmedEmail)) {
            onResult(false, "Please enter a valid email address.")
            return
        }

        try {
            auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fu = auth.currentUser
                    if (fu != null) {
                        loadUserProfileFromRemote(fu)
                    }
                    onResult(true, "Login successful")
                } else {
                    onResult(false, getFriendlyAuthErrorMessage(task.exception, isLogin = true))
                }
            }
        } catch (e: Throwable) {
            onResult(false, getFriendlyAuthErrorMessage(e, isLogin = true))
        }
    }

    fun register(name: String, phone: String, email: String, password: String, onResult: (Boolean, String) -> Unit) {
        val auth = firebaseAuth
        val fs = firestore
        if (auth == null) {
            onResult(false, "Firebase is not available on this device.")
            return
        }

        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        val trimmedEmail = email.trim()
        if (trimmedName.isBlank() || trimmedPhone.isBlank()) {
            onResult(false, "Please enter your full name and phone number.")
            return
        }
        if (!isValidEmail(trimmedEmail)) {
            onResult(false, "Please enter a valid email address.")
            return
        }
        if (password.length < 6) {
            onResult(false, "Password must be at least 6 characters long.")
            return
        }

        try {
            auth.createUserWithEmailAndPassword(trimmedEmail, password).addOnCompleteListener { createTask ->
                if (createTask.isSuccessful) {
                    val fu = auth.currentUser
                    if (fu != null) {
                        val profile = UserProfile(
                            id = fu.uid,
                            name = trimmedName,
                            phone = trimmedPhone,
                            email = trimmedEmail,
                            ffUid = fu.uid,
                            role = UserRole.USER
                        )
                        _currentUser.value = profile
                        if (fs != null) {
                            fs.collection("users").document(fu.uid).set(profile)
                        }
                        onResult(true, "Registration successful")
                    } else {
                        onResult(false, "Registration completed but the user profile could not be loaded.")
                    }
                } else {
                    onResult(false, getFriendlyAuthErrorMessage(createTask.exception, isLogin = false))
                }
            }
        } catch (e: Throwable) {
            onResult(false, getFriendlyAuthErrorMessage(e, isLogin = false))
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
            _currentPortalRole.value = UserRole.USER
            _isAuthenticated.value = false
        } catch (e: Throwable) {
            Log.w(TAG, "Signout failed: ${e.message}")
        }
    }

    fun resetPassword(email: String, onResult: (Boolean, String) -> Unit) {
        val auth = firebaseAuth
        if (auth == null) {
            onResult(false, "Firebase is not available.")
            return
        }
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            onResult(false, "Please enter your email.")
            return
        }
        auth.sendPasswordResetEmail(trimmedEmail).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, "Password reset email sent.")
            } else {
                onResult(false, getFriendlyAuthErrorMessage(task.exception, isLogin = true))
            }
        }
    }

    fun refreshAll() {
        // Attempt to refresh current user profile and basic lists from Firestore when available.
        try {
            val fu = firebaseAuth?.currentUser
            if (fu != null && firestore != null) {
                loadUserProfileFromRemote(fu)
                // Optionally load other collections like tournaments, notifications, leaderboard
                // For now, only refresh user profile to keep behavior deterministic.
            }
        } catch (e: Throwable) {
            Log.w(TAG, "refreshAll failed: ${e.message}")
        }
    }

    // --- Admin & Staff Actions ---

    fun updateRoomDetails(tournamentId: String, roomId: String, roomPass: String) {
        _tournaments.value = _tournaments.value.map {
            if (it.id == tournamentId) {
                it.copy(roomId = roomId, roomPassword = roomPass)
            } else it
        }
    }

    fun updateTournamentStatus(tournamentId: String, status: TournamentStatus) {
        _tournaments.value = _tournaments.value.map {
            if (it.id == tournamentId) {
                it.copy(status = status)
            } else it
        }
    }

    fun createTournament(tournament: Tournament) {
        _tournaments.value = listOf(tournament) + _tournaments.value
    }

    fun deleteTournament(tournamentId: String) {
        _tournaments.value = _tournaments.value.filterNot { it.id == tournamentId }
    }

    fun updateTournamentDetails(tournament: Tournament) {
        _tournaments.value = _tournaments.value.map {
            if (it.id == tournament.id) tournament else it
        }
    }

    fun addCategory(category: Category) {
        _categories.value = _categories.value + category
    }

    fun updateCategory(category: Category) {
        _categories.value = _categories.value.map {
            if (it.id == category.id) category else it
        }
    }

    fun deleteCategory(categoryId: String) {
        _categories.value = _categories.value.filterNot { it.id == categoryId }
    }

    fun updateConfig(config: AdminConfig) {
        _adminConfig.value = config
    }

    fun replyTicket(ticketId: String, replyText: String) {
        _tickets.value = _tickets.value.map {
            if (it.id == ticketId) {
                it.copy(reply = replyText, status = "RESOLVED")
            } else it
        }
    }

    fun addStaffUser(name: String, email: String, roleTitle: String) {
        val newStaff = StaffUser(name = name, email = email, roleTitle = roleTitle)
        _staffUsers.value = _staffUsers.value + newStaff
    }

    fun banUser(userId: String, isBanned: Boolean) {
        _allUsers.value = _allUsers.value.map {
            if (it.id == userId) it.copy(isBanned = isBanned) else it
        }
    }

    fun adjustUserWallet(userId: String, depositDelta: Double, winningDelta: Double, bonusDelta: Double) {
        _allUsers.value = _allUsers.value.map {
            if (it.id == userId) {
                it.copy(
                    depositBalance = maxOf(0.0, it.depositBalance + depositDelta),
                    winningBalance = maxOf(0.0, it.winningBalance + winningDelta),
                    bonusBalance = maxOf(0.0, it.bonusBalance + bonusDelta)
                )
            } else it
        }
        if (_currentUser.value.id == userId) {
            _currentUser.value = _currentUser.value.copy(
                depositBalance = maxOf(0.0, _currentUser.value.depositBalance + depositDelta),
                winningBalance = maxOf(0.0, _currentUser.value.winningBalance + winningDelta),
                bonusBalance = maxOf(0.0, _currentUser.value.bonusBalance + bonusDelta)
            )
        }
    }

    fun sendPushNotification(title: String, message: String) {
        val newNotif = AppNotification(
            title = title,
            message = message,
            timestamp = "Just Now",
            type = "PUSH",
            isRead = false
        )
        _notifications.value = listOf(newNotif) + _notifications.value
    }

    fun authenticateAdmin(loginId: String, passwordInput: String, onResult: (Boolean, String) -> Unit) {
        val cleanLogin = loginId.trim()
        val cleanPass = passwordInput.trim()

        if (cleanLogin.isBlank() || cleanPass.isBlank()) {
            onResult(false, "Please enter both email and password.")
            return
        }
        if (!isValidEmail(cleanLogin)) {
            onResult(false, "Please enter a valid admin email address.")
            return
        }

        val auth = firebaseAuth
        val fs = firestore
        if (auth == null || fs == null) {
            onResult(false, "Firebase authentication is not available on this device.")
            return
        }

        auth.signInWithEmailAndPassword(cleanLogin, cleanPass).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                onResult(false, getFriendlyAuthErrorMessage(task.exception, isLogin = true))
                return@addOnCompleteListener
            }

            val fu = auth.currentUser
            if (fu == null) {
                onResult(false, "Authentication completed but no account was returned.")
                return@addOnCompleteListener
            }

            fs.collection("users").document(fu.uid).get().addOnSuccessListener { snapshot ->
                val roleName = snapshot.getString("role")
                val role = roleName?.let { roleValue ->
                    runCatching { UserRole.valueOf(roleValue) }.getOrDefault(UserRole.USER)
                } ?: UserRole.USER
                if (role == UserRole.ADMIN) {
                    loadUserProfileFromRemote(fu)
                    _currentPortalRole.value = UserRole.ADMIN
                    onResult(true, "Admin authentication successful.")
                } else {
                    auth.signOut()
                    _isAuthenticated.value = false
                    onResult(false, "Access denied: this account is not an admin.")
                }
            }.addOnFailureListener { exception ->
                auth.signOut()
                _isAuthenticated.value = false
                onResult(false, getFriendlyAuthErrorMessage(exception, isLogin = true))
            }
        }
    }

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && email.trim().matches(emailRegex)
    }

    fun getFriendlyAuthErrorMessage(exception: Throwable?, isLogin: Boolean): String {
        return when (exception) {
            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL", "ERROR_INVALID_CREDENTIAL" -> if (isLogin) "Email not found or incorrect password." else "Please enter a valid email address."
                    "ERROR_WRONG_PASSWORD" -> "Incorrect password."
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists."
                    "ERROR_USER_NOT_FOUND" -> "Email not found."
                    "ERROR_USER_DISABLED" -> "This account has been disabled."
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again shortly."
                    else -> {
                        if (exception.errorCode == "ERROR_INVALID_CREDENTIAL" || exception.errorCode == "INVALID_LOGIN_CREDENTIALS") {
                            "Email not found or incorrect password."
                        } else {
                            exception.localizedMessage ?: "Authentication failed."
                        }
                    }
                }
            }
            else -> {
                if (exception?.message?.contains("INVALID_LOGIN_CREDENTIALS") == true) {
                    "Incorrect email or password."
                } else {
                    exception?.message ?: "Authentication failed."
                }
            }
        }
    }

    fun approveWithdrawal(txnId: String): Pair<Boolean, String> {
        _transactions.value = _transactions.value.map { txn ->
            if (txn.id == txnId && txn.type == TransactionType.WITHDRAW) {
                txn.copy(status = TransactionStatus.SUCCESS, title = "Withdrawal Approved & Sent")
            } else txn
        }
        val notif = AppNotification(
            title = "Withdrawal Dispatched! 💸",
            message = "Your payout request has been approved and sent to your UPI / Bank account.",
            type = "WALLET"
        )
        _notifications.value = listOf(notif) + _notifications.value
        return Pair(true, "Withdrawal approved successfully!")
    }

    fun rejectWithdrawal(txnId: String, reason: String = "Verification failed"): Pair<Boolean, String> {
        val txn = _transactions.value.find { it.id == txnId }
        if (txn != null && txn.type == TransactionType.WITHDRAW && txn.status == TransactionStatus.PENDING) {
            // Refund winning balance to user
            _allUsers.value = _allUsers.value.map { usr ->
                if (usr.id == txn.userId) usr.copy(winningBalance = usr.winningBalance + txn.amount) else usr
            }
            if (_currentUser.value.id == txn.userId) {
                _currentUser.value = _currentUser.value.copy(winningBalance = _currentUser.value.winningBalance + txn.amount)
            }
        }

        _transactions.value = _transactions.value.map { t ->
            if (t.id == txnId) {
                t.copy(status = TransactionStatus.REJECTED, description = "Rejected: $reason (Amount refunded)")
            } else t
        }
        val notif = AppNotification(
            title = "Withdrawal Request Rejected",
            message = "Your withdrawal request was rejected ($reason). Amount has been refunded to your winning wallet.",
            type = "WALLET"
        )
        _notifications.value = listOf(notif) + _notifications.value
        return Pair(true, "Withdrawal rejected and amount refunded.")
    }

    fun addStaffUserWithPermissions(name: String, email: String, roleTitle: String, permissions: List<String>) {
        val newStaff = StaffUser(
            name = name,
            email = email,
            roleTitle = roleTitle,
            permissions = if (permissions.isEmpty()) listOf("UPLOAD_ROOM", "UPDATE_RESULTS") else permissions
        )
        _staffUsers.value = _staffUsers.value + newStaff
    }

    fun deleteStaffUser(staffId: String) {
        _staffUsers.value = _staffUsers.value.filterNot { it.id == staffId }
    }
}
