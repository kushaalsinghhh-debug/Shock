package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.WinX7Repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class WinX7ViewModel : ViewModel() {

    val repository = WinX7Repository

    val currentUser: StateFlow<UserProfile> = repository.currentUser.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.currentUser.value
    )

    val currentPortalRole: StateFlow<UserRole> = repository.currentPortalRole.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.currentPortalRole.value
    )

    val isAuthenticated: StateFlow<Boolean> = repository.isAuthenticated.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.isAuthenticated.value
    )

    val categories: StateFlow<List<Category>> = repository.categories.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.categories.value
    )

    val tournaments: StateFlow<List<Tournament>> = repository.tournaments.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.tournaments.value
    )

    val joinedTournamentIds: StateFlow<Set<String>> = repository.joinedTournamentIds.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.joinedTournamentIds.value
    )

    val transactions: StateFlow<List<WalletTransaction>> = repository.transactions.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.transactions.value
    )

    val leaderboard: StateFlow<List<LeaderboardEntry>> = repository.leaderboard.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.leaderboard.value
    )

    val notifications: StateFlow<List<AppNotification>> = repository.notifications.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.notifications.value
    )

    val tickets: StateFlow<List<SupportTicket>> = repository.tickets.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.tickets.value
    )

    val coupons: StateFlow<List<Coupon>> = repository.coupons.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.coupons.value
    )

    val banners: StateFlow<List<BannerItem>> = repository.banners.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.banners.value
    )

    val announcements: StateFlow<List<Announcement>> = repository.announcements.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.announcements.value
    )

    val adminConfig: StateFlow<AdminConfig> = repository.adminConfig.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.adminConfig.value
    )

    val depositRequests: StateFlow<List<DepositRequest>> = repository.depositRequests.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.depositRequests.value
    )

    val staffUsers: StateFlow<List<StaffUser>> = repository.staffUsers.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.staffUsers.value
    )

    val allUsers: StateFlow<List<UserProfile>> = repository.allUsers.stateIn(
        viewModelScope, SharingStarted.Lazily, repository.allUsers.value
    )

    fun switchPortalRole(role: UserRole) {
        repository.switchPortalRole(role)
    }

    fun joinTournament(tournamentId: String, onResult: (Boolean, String) -> Unit) {
        val (success, message) = repository.joinTournament(tournamentId)
        onResult(success, message)
    }

    fun addDeposit(amount: Double) {
        repository.addDeposit(amount)
    }

    fun submitDepositRequest(
        amount: Double,
        txnId: String,
        upiId: String = "",
        paymentMode: String = "MANUAL_UPI",
        onResult: (Boolean, String) -> Unit
    ) {
        val (success, message) = repository.submitDepositRequest(amount, txnId, upiId, paymentMode)
        onResult(success, message)
    }

    fun approveDepositRequest(requestId: String, onResult: (Boolean, String) -> Unit) {
        val (success, message) = repository.approveDepositRequest(requestId)
        onResult(success, message)
    }

    fun rejectDepositRequest(requestId: String, reason: String = "", onResult: (Boolean, String) -> Unit) {
        val (success, message) = repository.rejectDepositRequest(requestId, reason)
        onResult(success, message)
    }

    fun requestWithdraw(amount: Double, onResult: (Boolean, String) -> Unit) {
        val (success, message) = repository.requestWithdraw(amount)
        onResult(success, message)
    }

    fun applyCoupon(code: String, onResult: (Boolean, String) -> Unit) {
        val (success, message) = repository.applyCoupon(code)
        onResult(success, message)
    }

    fun submitSupportTicket(subject: String, message: String) {
        repository.submitSupportTicket(subject, message)
    }

    fun updateProfile(name: String, avatarUrl: String = "", email: String = "", phone: String = "") {
        repository.updateProfile(name, avatarUrl, email, phone)
    }

    fun uploadProfileImage(uri: android.net.Uri, onResult: (Boolean, String) -> Unit) {
        repository.uploadProfileImage(uri, onResult)
    }

    fun updateRoomDetails(tournamentId: String, roomId: String, roomPass: String) {
        repository.updateRoomDetails(tournamentId, roomId, roomPass)
    }

    fun updateTournamentStatus(tournamentId: String, status: TournamentStatus) {
        repository.updateTournamentStatus(tournamentId, status)
    }

    fun createTournament(tournament: Tournament) {
        repository.createTournament(tournament)
    }

    fun deleteTournament(tournamentId: String) {
        repository.deleteTournament(tournamentId)
    }

    fun updateTournamentDetails(tournament: Tournament) {
        repository.updateTournamentDetails(tournament)
    }

    fun addCategory(name: String) {
        val newCat = Category(id = "cat-${System.currentTimeMillis() % 10000}", name = name)
        repository.addCategory(newCat)
    }

    fun updateCategory(category: Category) {
        repository.updateCategory(category)
    }

    fun deleteCategory(categoryId: String) {
        repository.deleteCategory(categoryId)
    }

    fun replyTicket(ticketId: String, reply: String) {
        repository.replyTicket(ticketId, reply)
    }

    fun addStaffUser(name: String, email: String, roleTitle: String) {
        repository.addStaffUser(name, email, roleTitle)
    }

    fun banUser(userId: String, isBanned: Boolean) {
        repository.banUser(userId, isBanned)
    }

    fun adjustUserWallet(userId: String, depositDelta: Double, winningDelta: Double, bonusDelta: Double) {
        repository.adjustUserWallet(userId, depositDelta, winningDelta, bonusDelta)
    }

    fun updateConfig(config: AdminConfig) {
        repository.updateConfig(config)
    }

    fun sendPushNotification(title: String, message: String) {
        repository.sendPushNotification(title, message)
    }

    fun authenticateAdmin(loginId: String, passwordInput: String, onResult: (Boolean, String) -> Unit) {
        repository.authenticateAdmin(loginId, passwordInput, onResult)
    }

    fun isValidEmail(email: String): Boolean {
        return repository.isValidEmail(email)
    }

    // --- Firebase auth helpers exposed to UI ---
    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        repository.signIn(email, password, onResult)
    }

    fun register(name: String, phone: String, email: String, password: String, onResult: (Boolean, String) -> Unit) {
        repository.register(name, phone, email, password, onResult)
    }

    fun resetPassword(email: String, onResult: (Boolean, String) -> Unit) {
        repository.resetPassword(email, onResult)
    }

    fun logout() {
        repository.signOut()
    }

    fun refreshAllData() {
        repository.refreshAll()
    }

    fun approveWithdrawal(txnId: String): Pair<Boolean, String> {
        return repository.approveWithdrawal(txnId)
    }

    fun rejectWithdrawal(txnId: String, reason: String): Pair<Boolean, String> {
        return repository.rejectWithdrawal(txnId, reason)
    }

    fun addStaffUserWithPermissions(name: String, email: String, roleTitle: String, permissions: List<String>) {
        repository.addStaffUserWithPermissions(name, email, roleTitle, permissions)
    }

    fun deleteStaffUser(staffId: String) {
        repository.deleteStaffUser(staffId)
    }
}
