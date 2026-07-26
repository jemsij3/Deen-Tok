package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.DeenTokViewModel
import com.example.viewmodel.Screen

data class PolicySection(
    val id: String,
    val title: String,
    val summary: String,
    val icon: ImageVector,
    val content: String,
    val islamicNote: String? = null,
    val isImportant: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyCenterScreen(
    viewModel: DeenTokViewModel,
    onBack: () -> Unit = { viewModel.setScreen(Screen.SETTINGS) }
) {
    val selectedTab by viewModel.selectedPolicyTab.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var expandAll by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showExportToast by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val tabs = listOf(
        Triple(0, "Community Guidelines", Icons.Default.MenuBook),
        Triple(1, "Terms of Service", Icons.Default.Gavel),
        Triple(2, "Privacy Policy", Icons.Default.Security),
        Triple(3, "Intellectual Property", Icons.Default.VerifiedUser)
    )

    val currentPolicySections = remember(selectedTab) {
        when (selectedTab) {
            0 -> getCommunityGuidelinesSections()
            1 -> getTermsOfServiceSections()
            2 -> getPrivacyPolicySections()
            3 -> getIntellectualPropertySections()
            else -> getCommunityGuidelinesSections()
        }
    }

    val filteredSections = remember(currentPolicySections, searchQuery) {
        if (searchQuery.isBlank()) {
            currentPolicySections
        } else {
            currentPolicySections.filter { section ->
                section.title.contains(searchQuery, ignoreCase = true) ||
                        section.summary.contains(searchQuery, ignoreCase = true) ||
                        section.content.contains(searchQuery, ignoreCase = true) ||
                        (section.islamicNote?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Terms & Policies",
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Deen Tok Policy Center • Effective July 2026",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("policy_center_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { expandAll = !expandAll },
                        modifier = Modifier.testTag("policy_toggle_expand_all_btn")
                    ) {
                        Icon(
                            imageVector = if (expandAll) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                            contentDescription = "Toggle Expand All",
                            tint = GoldYellow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        },
        containerColor = Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DarkNavy, SurfaceDark)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Legal & Community Trust Center",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Original Islamic social media standards built on honesty, dignity & justice.",
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("policy_search_input"),
                        placeholder = { Text("Search policies, guidelines or rules...", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanAccent) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextGray)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = SurfaceDark,
                            focusedContainerColor = DarkNavy,
                            unfocusedContainerColor = DarkNavy,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                }
            }

            // Tab Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { (index, title, icon) ->
                    val isSelected = selectedTab == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setPolicyTab(index) },
                        label = {
                            Text(
                                text = title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) Black else GoldYellow
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldYellow,
                            selectedLabelColor = Black,
                            containerColor = SurfaceDark,
                            labelColor = TextWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) GoldYellow else SurfaceDark
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.testTag("policy_tab_$index")
                    )
                }
            }

            // Document Title Banner & Search Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentTabTitle = tabs.find { it.first == selectedTab }?.second ?: "Policy Document"
                Text(
                    text = currentTabTitle.uppercase(),
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )

                if (searchQuery.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceDark
                    ) {
                        Text(
                            text = "${filteredSections.size} matches",
                            color = GoldYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Policy Section Accordions List
            if (filteredSections.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FindInPage, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching sections found for '$searchQuery'", color = TextGray, fontSize = 13.sp)
                        TextButton(onClick = { searchQuery = "" }) {
                            Text("Clear Search Filter", color = CyanAccent)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp, top = 4.dp)
                ) {
                    items(filteredSections, key = { it.id }) { section ->
                        PolicyAccordionCard(
                            section = section,
                            forceExpanded = expandAll || searchQuery.isNotEmpty()
                        )
                    }

                    // Contact & Support Footer Card inside List
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = DarkNavy,
                            border = BorderStroke(1.dp, SurfaceDark)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Questions or Policy Violation Reports?",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Our legal and moderation council is dedicated to preserving platform integrity according to Islamic values.",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showContactDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.testTag("policy_contact_support_btn")
                                    ) {
                                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Contact Legal & Policy Team", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Policy summary copied to clipboard!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(1.dp, CyanAccent),
                                        modifier = Modifier.testTag("policy_export_summary_btn")
                                    ) {
                                        Text("Export Summary", color = CyanAccent, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Contact Legal Team Modal Dialog
    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = GoldYellow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Deen Tok Legal & Policy Desk", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("For official policy inquiries, DMCA notices, or privacy access requests:", color = TextGray, fontSize = 12.sp)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("📧 Legal & DMCA: legal@deentok.app", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("🔒 Privacy & DPO: privacy@deentok.app", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("🛡️ Community Integrity: moderation@deentok.app", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("🏢 Head Office: Deen Tok Global Policy Council, Addis Ababa", color = TextWhite, fontSize = 11.sp)
                        }
                    }
                    Text("Response time is strictly within 24–48 business hours.", color = TextMuted, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showContactDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldYellow)
                ) {
                    Text("Close Desk Info", color = Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkNavy
        )
    }
}

@Composable
fun PolicyAccordionCard(
    section: PolicySection,
    forceExpanded: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val isCurrentlyExpanded = expanded || forceExpanded

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded }
            .testTag("policy_section_${section.id}"),
        shape = RoundedCornerShape(14.dp),
        color = DarkNavy,
        border = BorderStroke(
            1.dp,
            if (section.isImportant) GoldYellow.copy(alpha = 0.6f) else SurfaceDark
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (section.isImportant) GoldYellow.copy(alpha = 0.2f) else SurfaceDark
                    ) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = null,
                            tint = if (section.isImportant) GoldYellow else CyanAccent,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = section.title,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = section.summary,
                            color = TextGray,
                            fontSize = 11.sp,
                            maxLines = if (isCurrentlyExpanded) 5 else 1
                        )
                    }
                }

                Icon(
                    imageVector = if (isCurrentlyExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = isCurrentlyExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = SurfaceDark, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Islamic Alignment Box if present
                    if (!section.islamicNote.isNull_or_blank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Islamic Principle",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "Islamic Ethic & Principle",
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = section.islamicNote ?: "",
                                        color = TextWhite,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Policy Text Content
                    Text(
                        text = section.content,
                        color = TextWhite.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()

// ============================================================================
// ORIGINAL POLICY DOCUMENTS CREATED FROM SCRATCH FOR DEEN TOK
// ============================================================================

fun getCommunityGuidelinesSections(): List<PolicySection> {
    return listOf(
        PolicySection(
            id = "cg_1",
            title = "1. Platform Purpose & Vision",
            summary = "Fostering an authentic, uplifting Islamic social network centered on beneficial knowledge and unity.",
            icon = Icons.Default.CompassCalibration,
            islamicNote = "Based on the Qura'nic principle: 'Cooperate with one another in goodness and righteousness, and do not cooperate in sin and aggression.' (Al-Ma'idah 5:2)",
            content = """Deen Tok is designed to empower Muslims and ethical creators worldwide to share high-quality short video clips, educational lectures, Quranic recitations, nasheeds, daily reminders, and authentic community stories. 

Our mission is to build a vibrant digital sanctuary where social interaction brings benefit (Manfa'ah), strengthens character (Akhlaq), and promotes mutual respect among all people."""
        ),
        PolicySection(
            id = "cg_2",
            title = "2. Core Islamic Values & Conduct",
            summary = "Adherence to truthfulness (Sidq), kindness (Ihsan), dignity (Karama), and modesty (Haya).",
            icon = Icons.Default.VolunteerActivism,
            islamicNote = "Adhering to authentic Prophetic teachings: 'The Muslim is the one from whose tongue and hand people are safe.' (Sahih Al-Bukhari)",
            content = """All creators and viewers are expected to maintain good manners (Husn al-Khuluq) across all interactions on Deen Tok:

• Truthfulness (Sidq): Never post false statements, fake Hadiths, or fraudulent stories.
• Modesty (Haya): Ensure video dress, background visuals, and audio commentary maintain modesty and dignity.
• Respectful Speech: Refrain from backbiting (Gheebah), slander (Buhtan), offensive nicknames, or mocking others (Sukhriyyah).
• Noble Intent: Share content that inspires peace, knowledge, and family-friendly entertainment."""
        ),
        PolicySection(
            id = "cg_3",
            title = "3. Truthfulness & Religious Content Integrity",
            summary = "Strict standards against unverified fatwas, fake quotes, clickbait, and misleading religious claims.",
            icon = Icons.Default.AutoStories,
            islamicNote = "Preserving authentic religious knowledge with clarity, avoiding unverified fatwas or extremist interpretations.",
            content = """To maintain high religious and educational integrity:

• Verified Sources: Religious quotes, Quranic verses, and Hadith references must be cited clearly and accurately.
• No Unauthorized Fatwas: Creators may not issue binding religious decrees (fatwas) or declare individuals as disbelievers (Takfir). Present scholarly perspectives with humility and respect.
• Anti-Clickbait: Video titles and thumbnails must accurately represent the video content without deceptive trickery."""
        ),
        PolicySection(
            id = "cg_4",
            title = "4. Safety & Child Protection",
            summary = "Zero tolerance for child abuse, exploitation, underage danger, or inappropriate content.",
            icon = Icons.Default.ChildCare,
            isImportant = true,
            islamicNote = "Children are a sacred trust (Amanah). Protecting minors is a non-negotiable priority on Deen Tok.",
            content = """Deen Tok strictly enforces absolute protections for minors (under age 18):

• Zero Exploitation: Any form of child sexual abuse material (CSAM), grooming, or physical endangerment results in immediate permanent ban and direct notification to law enforcement.
• Underage Safety: Minors are prohibited from participating in dangerous stunts, extreme physical challenges, or sharing personal private details.
• Family Controls: Parents and guardians can utilize safety filters and report any concerning content 24/7."""
        ),
        PolicySection(
            id = "cg_5",
            title = "5. Harassment & Anti-Cyberbullying",
            summary = "Prohibiting targeted attacks, doxxing, character assassination, mass trolling, or stalking.",
            icon = Icons.Default.MoodBad,
            islamicNote = "Honoring human dignity: 'O you who believe, let not a people ridicule another people...' (Al-Hujurat 49:11)",
            content = """We forbid any conduct aimed at intimidating, humiliating, or defaming individuals:

• No Doxxing: Publishing private addresses, phone numbers, emails, or real identity details without explicit written consent is illegal.
• No Mass Trolling: Organised raids on comment sections, video downvote campaigns, or toxic mobbing are banned.
• No Stalking: Unwanted continuous messaging, persistent tag harassment, or unwanted live stream intrusion is prohibited."""
        ),
        PolicySection(
            id = "cg_6",
            title = "6. Hate Speech & Non-Discrimination",
            summary = "Strict prohibition against racism, tribalism, sectarian hostility, or xenophobia.",
            icon = Icons.Default.GroupOff,
            isImportant = true,
            islamicNote = "Universal equality: 'All mankind is from Adam and Eve; an Arab has no superiority over a non-Arab...' (Farewell Sermon)",
            content = """Deen Tok welcomes users from all ethnic, national, cultural, and peaceful backgrounds. We strictly ban:

• Racial or Tribal Prejudices: Promoting superiority or inferiority based on lineage, skin color, or ethnicity.
• Sectarian Hatred: Inciting hostility, violence, or vitriol against specific religious groups or schools of thought.
• Dehumanizing Language: Describing any group of people using slurs, animalistic metaphors, or threats."""
        ),
        PolicySection(
            id = "cg_7",
            title = "7. Violence, Weapons & Extremism",
            summary = "Banning promotion of physical violence, terrorism, illegal weapons, self-harm, or extreme acts.",
            icon = Icons.Default.Dangerous,
            isImportant = true,
            content = """Deen Tok maintains a zero-tolerance policy regarding violent extremism and physical harm:

• No Violent Extremism: Support, recruitment, or glorification of militant organizations, terror groups, or violent insurgents is banned.
• No Self-Harm: Content encouraging suicide, eating disorders, or self-inflicted injuries will be removed immediately with emergency helpline support provided.
• No Weapon Trade: Promoting illegal firearms, explosive recipes, or dangerous tactical weapons is prohibited."""
        ),
        PolicySection(
            id = "cg_8",
            title = "8. Fraud, Scams & Commercial Integrity",
            summary = "Prohibiting pyramid schemes, fraudulent donation drives, crypto traps, and deceptive sales.",
            icon = Icons.Default.MonetizationOn,
            islamicNote = "Prohibiting unlawful consumption of wealth: 'Do not consume one another's wealth unjustly.' (Al-Baqarah 2:188)",
            content = """Protecting community members from financial exploitation:

• Fake Charity Drives: Unauthorized donation requests claiming to collect Zakat or Sadaqah without verified credentials are illegal.
• Financial Scams: High-yield investment schemes, cryptocurrency pump-and-dump traps, or get-rich-quick promises are banned.
• Phishing: Account theft links or deceptive login pages will result in instant ban and domain blacklisting."""
        ),
        PolicySection(
            id = "cg_9",
            title = "9. Misinformation, Health Fake News & Spam",
            summary = "Preventing panic-inducing rumors, medical misinformation, and automated bot manipulation.",
            icon = Icons.Default.ReportProblem,
            content = """Maintaining an accurate and trustworthy information environment:

• Panic Rumors: Fabricating public safety scares, fake natural disaster alerts, or false government announcements.
• Dangerous Health Claims: Promoting unproven, lethal medical cures or fake remedies that endanger lives.
• Spam Manipulation: Utilizing automated scripts or bots to inflate video views, likes, comments, or followers."""
        ),
        PolicySection(
            id = "cg_10",
            title = "10. Violations, Escalation, Bans & Appeals",
            summary = "Transparent 3-tier warning system, temporary restrictions, permanent bans, and appeal process.",
            icon = Icons.Default.Rule,
            isImportant = true,
            content = """Deen Tok operates a fair, transparent moderation framework:

• 1st Violation (Warning): Formal educational notice issued with policy reference.
• 2nd Violation (Temporary Mute/Restriction): 7 to 14-day restriction on posting videos, live streams, or comments.
• 3rd Violation (Permanent Account Ban): Complete termination of profile, follower list, and earnings access.
• Immediate Permanent Ban: Severe offenses like child abuse material, terrorism, or major fraud result in instant permanent bans.
• Right to Appeal: Creators may submit an appeal within 30 days via support@deentok.app. Appeals are reviewed by a human moderation committee within 5 business days."""
        )
    )
}

fun getTermsOfServiceSections(): List<PolicySection> {
    return listOf(
        PolicySection(
            id = "tos_1",
            title = "1. Acceptance of Terms",
            summary = "Binding legal agreement between you and Deen Tok upon downloading or using the platform.",
            icon = Icons.Default.AssignmentTurnedIn,
            content = """By registering an account, downloading, browsing, or using Deen Tok ('Platform', 'We', 'Us'), you enter into a legally binding agreement to abide by these Terms of Service, our Community Guidelines, Privacy Policy, and Intellectual Property Policy.

If you do not agree to any part of these terms, you must immediately discontinue use of the platform and delete your account."""
        ),
        PolicySection(
            id = "tos_2",
            title = "2. Eligibility & Account Creation",
            summary = "Minimum age requirement of 13 years, parental oversight for minors, and credential security.",
            icon = Icons.Default.PersonAdd,
            content = """• Minimum Age: You must be at least 13 years of age (or the legal age required in your jurisdiction) to create an account. Minors between 13 and 18 must have parental or legal guardian consent.
• Accurate Information: You agree to provide truthful email addresses, phone numbers, and profile details during registration.
• Account Security: You are solely responsible for keeping your password and access tokens confidential. Any actions taken through your account are your legal responsibility."""
        ),
        PolicySection(
            id = "tos_3",
            title = "3. User Responsibilities & Prohibited Acts",
            summary = "Obligation to obey local laws, refrain from reverse engineering, or exploiting platform code.",
            icon = Icons.Default.Verified,
            content = """Users agree never to engage in any of the following activities:

1. Reverse engineering, decompiling, or attempting to extract source code from the Deen Tok application.
2. Bypassing rate limits, security tokens, or automated content filtering systems.
3. Using web scrapers, spiders, or automated bots to extract user profiles, video databases, or comments.
4. Attempting unauthorized access to administrative controls, servers, or user database records."""
        ),
        PolicySection(
            id = "tos_4",
            title = "4. User-Generated Content & License Grant",
            summary = "Creators retain full ownership of original videos while granting Deen Tok a distribution license.",
            icon = Icons.Default.VideoLibrary,
            islamicNote = "Honoring individual ownership and intellectual property rights in full accordance with Islamic justice.",
            content = """• You Retain Ownership: You retain all copyright and legal ownership of original videos, audio clips, and thumbnails that you create and upload to Deen Tok.
• Worldwide License Grant: By uploading content, you grant Deen Tok a non-exclusive, worldwide, royalty-free, sublicensable license to host, stream, reproduce, reformat, display, and distribute your content solely for the operation and promotion of the Platform.
• Warranty of Originality: You represent and warrant that you possess all necessary rights, licenses, and permissions for all audio, music, recitations, and visual elements included in your content."""
        ),
        PolicySection(
            id = "tos_5",
            title = "5. Platform Rights & Governance",
            summary = "Deen Tok reserves rights to moderate, feature, restrict, or modify service offerings.",
            icon = Icons.Default.AdminPanelSettings,
            content = """Deen Tok reserves the exclusive right to:

• Review, flag, or remove any video, live stream, or comment that violates our policies or applicable laws.
• Modify, update, pause, or terminate features, APIs, or user interfaces at any time without prior notice.
• Re-assign or revoke usernames that impersonate public figures, scholars, or organizations."""
        ),
        PolicySection(
            id = "tos_6",
            title = "6. Virtual Economy, Gifts & DT Coins",
            summary = "Terms governing DT Coins, virtual gifts, creator payouts, non-refundable coin balances, and anti-fraud.",
            icon = Icons.Default.Paid,
            isImportant = true,
            islamicNote = "Ensuring transparent, clear transaction rules with no hidden gharar (uncertainty) or illegal gambling mechanics.",
            content = """• DT Coins: DT Coins are virtual tokens used exclusively within the Deen Tok platform to send appreciation gifts to content creators during short videos or LIVE streams.
• Non-Refundable Purchase: All DT Coin purchases are final and non-refundable once credited to your account balance, except where mandated by local consumer protection laws.
• Creator Rewards: Creators receive virtual gift proceeds converted into platform rewards according to transparent payout ratios, subject to account verification and anti-money laundering compliance.
• No Gambling: Virtual gifts represent voluntary appreciation tokens and may never be used for gambling, betting, or illegal financial speculation."""
        ),
        PolicySection(
            id = "tos_7",
            title = "7. Disclaimer of Warranties",
            summary = "Service provided 'As Is' and 'As Available' without implied guarantees of uninterrupted uptime.",
            icon = Icons.Default.Warning,
            content = """Deen Tok provides its application, server feeds, and services on an 'AS IS' and 'AS AVAILABLE' basis. To the maximum extent permitted by law:

• We make no express or implied warranties regarding uninterrupted uptime, server error elimination, or fitness for a specific purpose.
• Opinions, religious commentary, and statements expressed in user-generated videos belong solely to the respective creators and do not reflect the official views of Deen Tok Platform."""
        ),
        PolicySection(
            id = "tos_8",
            title = "8. Limitation of Liability",
            summary = "Limitation of damages for indirect losses, data disruptions, or third-party interactions.",
            icon = Icons.Default.Security,
            content = """In no event shall Deen Tok, its directors, employees, or technical partners be liable for any indirect, incidental, special, consequential, or punitive damages arising from:

• Your access to, or inability to access, the platform.
• Any conduct or content of any third party or user on the service.
• Unauthorized access, use, or alteration of your profile or video transmissions."""
        ),
        PolicySection(
            id = "tos_9",
            title = "9. Governing Law & Dispute Resolution",
            summary = "Fair, amicable dispute resolution and arbitration principles.",
            icon = Icons.Default.Balance,
            content = """• Amicable Resolution (Sulh): In the spirit of Islamic brotherhood and fairness, parties agree to first attempt to resolve any dispute or grievance through direct negotiation and good-faith mediation.
• Legal Jurisdiction: Any unresolved legal disputes shall be governed by applicable laws and submitted to competent arbitration courts."""
        )
    )
}

fun getPrivacyPolicySections(): List<PolicySection> {
    return listOf(
        PolicySection(
            id = "pp_1",
            title = "1. Information We Collect",
            summary = "Detailed breakdown of account data, device metrics, uploaded content, and usage logs.",
            icon = Icons.Default.Storage,
            content = """We collect minimal data necessary to provide a smooth, secure, and personalized experience:

• Account Information: Username, email address, phone number, profile photo, bio description, and language preference.
• Uploaded Content: Videos, voice clips, captions, thumbnails, comments, and direct chat messages you transmit.
• Technical & Device Data: Device model, operating system version, unique app installation tokens, IP address, screen resolution, and crash log analytics.
• Usage Metrics: Videos viewed, watch time duration, likes, shares, bookmark lists, and virtual gift transactions."""
        ),
        PolicySection(
            id = "pp_2",
            title = "2. How We Use Your Data",
            summary = "Personalizing video recommendations, facilitating creator gifts, and maintaining safety.",
            icon = Icons.Default.DataUsage,
            content = """Your data is used strictly for legitimate platform purposes:

• Delivering & Personalizing Feeds: Showing relevant Islamic educational content, creator videos, and language preferences.
• Economy & Creator Payouts: Accurately calculating DT Coin balances, virtual gift histories, and creator reward distributions.
• Security & Fraud Detection: Detecting spam bots, unauthorized login attempts, and policy-violating uploads.
• Platform Improvements: Analyzing performance logs to fix app crashes, optimize video streaming, and enhance UI speed."""
        ),
        PolicySection(
            id = "pp_3",
            title = "3. Zero Sale of Personal Data",
            summary = "Deen Tok NEVER sells, rents, or monetizes user personal data to ad brokers or third parties.",
            icon = Icons.Default.Lock,
            isImportant = true,
            islamicNote = "Preserving user trust (Amanah) and privacy as a fundamental moral obligation.",
            content = """We operate under an absolute privacy commitment:

• No Data Selling: Deen Tok NEVER sells your personal identification, phone numbers, or private message history to third-party ad brokers, market researchers, or commercial aggregators.
• Essential Service Providers: We only share encrypted data with trusted cloud infrastructure partners (e.g. video hosting servers) under strict non-disclosure and data protection agreements."""
        ),
        PolicySection(
            id = "pp_4",
            title = "4. Data Storage, Encryption & Retention",
            summary = "Industry-standard SSL/TLS transit encryption, secure database storage, and retention policies.",
            icon = Icons.Default.Key,
            content = """• Encryption in Transit: All data transmitted between the Deen Tok mobile app and backend servers is encrypted using standard TLS 1.3 encryption.
• Secure Storage: User credentials, password hashes, and auth tokens are stored using salted cryptographic hashing algorithms.
• Retention Period: We retain user data only for as long as your account remains active or as required for legal compliance and fraud prevention."""
        ),
        PolicySection(
            id = "pp_5",
            title = "5. Children's Privacy Controls",
            summary = "Special safeguards for young users, restricting targeted processing, and parental requests.",
            icon = Icons.Default.ChildFriendly,
            content = """Deen Tok does not knowingly collect personal information from children under 13 without verifiable parental authorization:

• Enhanced Privacy Default: Accounts identified as belonging to minors have restrictive privacy settings enabled by default.
• Parental Erasure: Parents or legal guardians may contact privacy@deentok.app to review, request deletion, or halt processing of their child's data."""
        ),
        PolicySection(
            id = "pp_6",
            title = "6. Your Data Rights & Control",
            summary = "Rights to access, edit, export activity history, or permanently delete your account & data.",
            icon = Icons.Default.ManageAccounts,
            isImportant = true,
            content = """You possess full control over your personal data on Deen Tok:

• Right to Access & Export: You may request a complete downloadable file of your account details, videos, comments, and transaction logs.
• Right to Rectification: You can edit your profile, username, email, and preferences at any time via Settings.
• Right to Erasure (Account Deletion): You may permanently delete your account and remove all personal data directly inside Settings or by emailing privacy@deentok.app."""
        ),
        PolicySection(
            id = "pp_7",
            title = "7. Permanent Account & Data Deletion Procedure",
            summary = "In-app automated deletion button and 30-day purge cycle details.",
            icon = Icons.Default.DeleteForever,
            content = """To permanently delete your Deen Tok account and remove all associated data:

1. Open Deen Tok Settings -> Account Settings.
2. Tap 'Delete Account & Remove Data' and confirm your password.
3. Your account will immediately enter a 30-day grace period (in case of accidental deletion).
4. After 30 days, all video files, database records, comments, and profile data are permanently purged from active production servers."""
        )
    )
}

fun getIntellectualPropertySections(): List<PolicySection> {
    return listOf(
        PolicySection(
            id = "ip_1",
            title = "1. Respect for Intellectual Property",
            summary = "Commitment to protecting original works of videographers, reciters, nasheed artists, and scholars.",
            icon = Icons.Default.Copyright,
            islamicNote = "Respecting creator effort and property rights: 'Give the worker his wages before his sweat dries.' (Sunan Ibn Majah)",
            content = """Deen Tok respects the intellectual property rights of all content creators, scholars, publishers, reciters, and artists worldwide.

We strictly prohibit users from uploading, streaming, or sharing videos, audio recordings, or visual art that infringes upon third-party copyrights, trademarks, or proprietary rights."""
        ),
        PolicySection(
            id = "ip_2",
            title = "2. User Content Ownership & Attribution",
            summary = "Creators own their original audio/video works and are encouraged to cite audio sources.",
            icon = Icons.Default.RecordVoiceOver,
            content = """• You Own Your Original Work: As a creator, you retain full ownership of videos you film, audio you record, and digital graphics you design.
• Proper Attribution: When using permissible background audio clips, Quranic recitations, or educational speech excerpts, creators must provide clear credit to original reciters or speakers in the video description."""
        ),
        PolicySection(
            id = "ip_3",
            title = "3. Reporting Copyright Infringement (DMCA & Global Takedown)",
            summary = "Step-by-step submission guide for legal copyright holders to submit formal takedown requests.",
            icon = Icons.Default.Report,
            isImportant = true,
            content = """If you are a copyright owner or authorized legal agent and believe your protected work has been copied or posted on Deen Tok without authorization, you may submit a formal Takedown Notice to legal@deentok.app containing:

1. Full legal name, physical address, phone number, and official email address.
2. Identification of the copyrighted work claimed to have been infringed (e.g. original video link or registration number).
3. Exact URL or Deen Tok Video ID location of the allegedly infringing material.
4. A statement made under penalty of perjury that you have a good-faith belief that the use is unauthorized.
5. Physical or electronic signature of the copyright owner or authorized representative."""
        ),
        PolicySection(
            id = "ip_4",
            title = "4. Counter-Notification Procedure",
            summary = "Right of affected creators to submit a legal counter-notice if content was mistakenly removed.",
            icon = Icons.Default.Undo,
            content = """If your video was removed due to a copyright notice and you believe this was an error, mistaken identification, or covered by fair use, you may submit a Counter-Notification to legal@deentok.app including:

1. Your full name, account username, email, and physical address.
2. Identification of the specific video ID removed and its prior location.
3. A statement under penalty of perjury that you have a good-faith belief the material was removed as a result of mistake or misidentification.
4. Consent to legal jurisdiction for copyright dispute resolution."""
        ),
        PolicySection(
            id = "ip_5",
            title = "5. Repeat Infringer Policy (3-Strikes System)",
            summary = "Strict policy terminating accounts that repeatedly violate intellectual property rights.",
            icon = Icons.Default.Block,
            isImportant = true,
            content = """Deen Tok enforces a strict Repeat Infringer Policy:

• Strike 1: Content removed; creator receives formal copyright warning and mandatory copyright education token.
• Strike 2: Content removed; account restricted from posting videos or live streams for 14 days.
• Strike 3: Permanent termination of user account, removal of creator channel, and forfeiture of unverified gift balances."""
        ),
        PolicySection(
            id = "ip_6",
            title = "6. Trademark & Brand Asset Guidelines",
            summary = "Rules regarding Deen Tok logos, trademarks, and prohibiting commercial impersonation.",
            icon = Icons.Default.VerifiedUser,
            content = """The Deen Tok name, logo, custom icons, and visual branding assets are protected trademarks of Deen Tok Platform:

• No Impersonation: You may not use the Deen Tok logo, name, or official badges as your profile icon to mislead users into believing your channel is an official platform account.
• Commercial Permission: Any commercial licensing of Deen Tok brand assets requires explicit written consent from brand@deentok.app."""
        )
    )
}
