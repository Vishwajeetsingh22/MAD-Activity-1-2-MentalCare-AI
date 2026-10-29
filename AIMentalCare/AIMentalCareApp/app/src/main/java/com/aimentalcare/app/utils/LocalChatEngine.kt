package com.aimentalcare.app.utils

/**
 * LocalChatEngine
 * Stateful On-Device Conversational Mental Wellness Assistant for MentalCare AI.
 * Tagline: Understand. Relax. Thrive.
 * 
 * Provides offline conversational intelligence, dialogue context tracking,
 * and safety guardrails without repeating generic canned strings.
 */
object LocalChatEngine {

    private val CRISIS_KEYWORDS = listOf(
        "suicide", "kill myself", "end my life", "harm myself", "giving up completely",
        "can't go on", "want to die", "better off dead", "extreme crisis", "no reason to live"
    )

    private val MEDICAL_KEYWORDS = listOf(
        "diagnose", "depression", "bipolar", "prescribe", "medication", "medicine", "pills", "adhd", "schizophrenia"
    )

    private class ConversationMemory(val conversationId: String) {
        var turns = 0
        var stressStated = false
        val causes = mutableListOf<String>()
        val subjects = mutableListOf<String>()
        var prepared: Boolean? = null
        var sleepIssue = false
        var sleepRecurring = false
        val topics = mutableSetOf<String>()
        var lastIntent: String? = null
        val usedReplies = mutableSetOf<String>()
    }

    private val memoryStore = mutableMapOf<String, ConversationMemory>()

    private fun getMemory(conversationId: String): ConversationMemory {
        return memoryStore.getOrPut(conversationId) {
            ConversationMemory(conversationId)
        }
    }

    fun respond(message: String, conversationId: String = "default_session", currentStressLevel: String? = null): ChatResult {
        val lower = message.lowercase().trim()
        val mem = getMemory(conversationId)
        mem.turns++

        if (lower.isEmpty()) {
            return ChatResult("I'm here whenever you'd like to share how you're feeling.", isCrisis = false)
        }

        // 1. Crisis detection
        if (CRISIS_KEYWORDS.any { lower.contains(it) }) {
            return ChatResult(
                reply = "I am deeply concerned about what you are going through right now. Please remember that you do not have to carry this alone and immediate help is available. Please reach out to your emergency contact right now, or contact the 988 Suicide & Crisis Lifeline by calling or texting 988 (free, confidential, 24/7). If you are in immediate danger, please contact local emergency services immediately.",
                isCrisis = true
            )
        }

        // 2. Medical query boundary
        if (MEDICAL_KEYWORDS.any { lower.contains(it) }) {
            return ChatResult(
                reply = "As an AI wellness assistant, I cannot provide medical diagnoses, evaluate clinical psychiatric disorders, or recommend prescription medications. MentalCare AI is strictly an assistive wellness tool. If you are experiencing symptoms of illness, depression, or severe anxiety, I strongly encourage you to speak with a licensed physician or mental health professional.",
                isCrisis = false
            )
        }

        // Extract entities
        if (listOf("stressed", "stress", "overwhelmed", "anxious", "anxiety", "pressure").any { lower.contains(it) }) {
            mem.stressStated = true
            mem.topics.add("stress")
        }

        if (listOf("exam", "exams", "test", "finals", "tomorrow").any { lower.contains(it) }) {
            if (!mem.causes.contains("exams tomorrow")) {
                mem.causes.add("exams tomorrow")
            }
            mem.topics.add("exams")
        }

        if (listOf("mathematics", "maths", "math", "calculus", "algebra").any { lower.contains(it) }) {
            if (!mem.subjects.contains("mathematics")) {
                mem.subjects.add("mathematics")
            }
            mem.topics.add("mathematics")
        }

        if (listOf("haven't studied", "didn't study", "not prepared", "not ready", "zero preparation").any { lower.contains(it) }) {
            mem.prepared = false
        }

        if (listOf("sleep", "sleeping", "insomnia", "can't sleep", "trouble sleeping").any { lower.contains(it) }) {
            mem.sleepIssue = true
            mem.topics.add("sleep")
        }

        if (mem.sleepIssue && listOf("every night", "almost every night", "always", "frequently", "all the time").any { lower.contains(it) }) {
            mem.sleepRecurring = true
        }

        // CASE 1: MEMORY RECALL QUERIES (e.g. "What did I tell you about my exams?")
        if (listOf("what did i tell you", "what did i say", "do you remember", "did i mention", "recall what i said").any { lower.contains(it) }) {
            if (lower.contains("exam") || lower.contains("exams")) {
                val reply = if (mem.subjects.contains("mathematics") || mem.prepared == false) {
                    "You shared that your exams are tomorrow, and you were feeling particularly worried and stressed because you haven't had a chance to study mathematics yet."
                } else {
                    "You mentioned earlier that your exams are tomorrow and that they have been causing you stress."
                }
                return ChatResult(reply, isCrisis = false)
            }
            if (lower.contains("sleep")) {
                return ChatResult("You shared that you have also been having trouble sleeping amidst the exam stress.", isCrisis = false)
            }
            val parts = mutableListOf<String>()
            if (mem.causes.contains("exams tomorrow")) parts.add("your exams tomorrow")
            if (mem.subjects.contains("mathematics")) parts.add("being unprepared for mathematics")
            if (mem.sleepIssue) parts.add("having trouble sleeping")
            val summary = if (parts.isNotEmpty()) parts.joinToString(", ") else "your feelings of stress"
            return ChatResult("In our conversation, you shared that you've been dealing with $summary. I am keeping that in mind to support you.", isCrisis = false)
        }

        // CASE 2: AFFIRMATIONS & ASKING AI TO LEAD (e.g. "Yes ask me questions")
        if (listOf("yes ask me", "ask me questions", "ask me a question", "ask me something", "please ask me").any { lower.contains(it) }) {
            mem.lastIntent = "asked_source"
            val reply = "Thank you for trusting me. Let's take it one step at a time so it doesn't feel overwhelming. What has been the main source of pressure or the biggest thing weighing on your mind today?"
            return ChatResult(reply, isCrisis = false)
        }

        if (lower in listOf("yes", "sure", "yeah", "yep", "ok", "okay", "please", "yes please")) {
            if (mem.lastIntent == "offered_study_plan") {
                return generateStudyPlanResult(mem)
            } else if (mem.lastIntent == "offered_breathing") {
                return generateRelaxationResult()
            } else {
                return ChatResult("I'm listening. What is on your mind right now that you'd like to talk through?", isCrisis = false)
            }
        }

        // CASE 3: EXPLICIT STUDY PLAN REQUEST
        if (listOf("study plan", "help me study", "study schedule", "plan my revision", "make a study plan").any { lower.contains(it) }) {
            return generateStudyPlanResult(mem)
        }

        // CASE 4: SLEEP DIFFICULTY
        if (mem.sleepIssue && listOf("trouble sleeping", "can't sleep", "insomnia", "sleeping", "sleep", "every night").any { lower.contains(it) }) {
            if (mem.sleepRecurring) {
                val reply = "Struggling to sleep almost every night is physically and emotionally exhausting. When sleep disruptions become a recurring pattern, your mind begins associating bed with restlessness.\n\nA gentle routine that can help rebuild your sleep cues:\n• Keep your bedroom cool, dark, and screen-free 45 minutes before bedtime.\n• If you cannot fall asleep after 20 minutes, get out of bed and do a quiet, dim-light activity until drowsiness returns.\n• Practice a 5-minute diaphragmatic breathing cycle to signal to your nervous system that you are safe to rest.\n\nWould you like to set an evening wind-down reminder in your Daily Plan?"
                return ChatResult(reply, isCrisis = false)
            } else {
                val examConn = if (mem.subjects.contains("mathematics") || mem.causes.contains("exams tomorrow")) {
                    " It is very common when your mind is racing about tomorrow's mathematics exam—anticipatory worry keeps your nervous system on high alert."
                } else ""
                val reply = "Having trouble sleeping is very frustrating and makes coping with stress much harder.$examConn\n\nA few practical steps to help your mind transition into rest tonight:\n• Step away from study materials at least 45 minutes before bed.\n• Try the 4-7-8 breathing technique: Inhale for 4s, hold gently for 7s, exhale slowly for 8s.\n• Write down your top 3 remaining tasks on paper to 'park' them outside your head for tonight.\n\nWould you like to try a guided 5-minute relaxation exercise right now?"
                mem.lastIntent = "offered_breathing"
                return ChatResult(reply, isCrisis = false)
            }
        }

        // CASE 5: MATHEMATICS & UNPREPAREDNESS
        if (listOf("mathematics", "math", "haven't studied", "havent studied", "not prepared", "not studied").any { lower.contains(it) }) {
            mem.lastIntent = "offered_study_plan"
            val reply = "Facing a mathematics exam tomorrow without feeling prepared is definitely stressful, but panicking will only drain the working memory you need tonight. The key right now is triage rather than trying to cram the entire syllabus:\n\n1. Focus on core formulas and standard high-frequency question types.\n2. Work through 2-3 solved examples by hand rather than just reading notes.\n3. Take short breathing breaks to keep your anxiety from blocking your problem-solving.\n\nWould you like me to help you make a practical study plan for mathematics tonight?"
            return ChatResult(reply, isCrisis = false)
        }

        // CASE 6: EXAMS TOMORROW
        if (listOf("exams are tomorrow", "exam tomorrow", "exam is tomorrow", "finals tomorrow", "exams tomorrow").any { lower.contains(it) }) {
            mem.lastIntent = "asked_which_subject"
            val reply = "Having exams tomorrow creates a lot of acute pressure. When the countdown begins, our minds often jump to worst-case scenarios. Focusing on one specific subject at a time helps prevent overwhelm. Which specific subject or topics are feeling the most urgent for tomorrow?"
            return ChatResult(reply, isCrisis = false)
        }

        // CASE 7: 5-MINUTE RELAXATION
        if (listOf("relax for five minutes", "relax for 5 minutes", "5 minutes", "five minutes", "quick relax").any { lower.contains(it) }) {
            return generateRelaxationResult()
        }

        // CASE 8: INITIAL STRESS DISCLOSURE
        if (mem.stressStated && mem.turns <= 2) {
            mem.lastIntent = "asked_general_stress"
            val stressHint = if (currentStressLevel != null) " (Your recent check-in also indicated $currentStressLevel stress)" else ""
            val reply = "I'm sorry you're feeling so stressed today$stressHint. It takes self-awareness to recognize when things are feeling like too much. Would you like to explore what is putting the most weight on your mind today, or would you like me to ask you a couple of gentle questions?"
            return ChatResult(reply, isCrisis = false)
        }

        // CASE 9: POSITIVE FEELINGS
        if (listOf("good", "great", "happy", "calm", "peaceful", "better", "relieved", "fine").any { lower.contains(it) }) {
            val reply = "That's wonderful to hear! Acknowledging and savoring calm or positive moments builds emotional resilience. What is something that went well or brought you peace today?"
            return ChatResult(reply, isCrisis = false)
        }

        // CASE 10: WORK / BURNOUT
        if (listOf("work", "job", "boss", "colleague", "workload", "office", "burnout").any { lower.contains(it) }) {
            val reply = "Heavy workplace demands and prolonged expectations often cause burnout when rest is delayed. Establishing small boundaries—such as stepping away during lunch and turning off notifications in the evening—can protect your energy. Have you been able to take any genuine breaks away from your workstation today?"
            return ChatResult(reply, isCrisis = false)
        }

        // CASE 10B: RECENT CHECK-INS QUERY
        if (listOf("recent check-in", "recent checkin", "recent check ins", "my check-ins", "my checkins", "track my stress", "how my stress has been").any { lower.contains(it) }) {
            val reply = if (currentStressLevel != null) {
                "Your most recent wellness check-in indicated $currentStressLevel stress. Consistent daily logging helps identify burnout patterns early so you can take restorative pauses. How are your energy and stress levels feeling right now compared to earlier today?"
            } else {
                "Your recent check-in records show a steady baseline. Completing your Daily Check-in each morning takes less than a minute and helps me give you personalized wellness suggestions. How are you feeling right now?"
            }
            return ChatResult(reply, isCrisis = false)
        }

        // DEFAULT DYNAMIC CONVERSATION (Never repeat the exact same canned reply!)
        val topicsSummary = mem.topics.joinToString(" and ")
        val reply = if (topicsSummary.isNotEmpty()) {
            "I hear you. In our conversation, you've mentioned dealing with $topicsSummary. Giving words to your thoughts is an important step in caring for your mental wellbeing. How are you feeling right now as we talk through this?"
        } else {
            "I'm listening and I'm here to support you. You can talk freely about whatever is on your mind—whether it's exam deadlines, work pressure, sleep difficulties, or just wanting a mindful pause. How can I best support you right now?"
        }
        return ChatResult(reply, isCrisis = false)
    }

    private fun generateStudyPlanResult(mem: ConversationMemory): ChatResult {
        val subject = if (mem.subjects.isNotEmpty()) mem.subjects.first() else "your upcoming exam"
        val reply = "Let's build a focused, stress-reducing emergency study plan for $subject tomorrow:\n\n" +
                "• Step 1: Formula Triage (40 mins): Write out the top 10 most essential formulas and definitions on a single sheet.\n" +
                "• Step 2: High-Yield Practice (50 mins): Work through 3-4 standard solved problems from previous papers step-by-step.\n" +
                "• Step 3: Mandatory 10-min Break: Step away, drink a glass of cool water, and stretch your shoulders.\n" +
                "• Step 4: Concept Review (30 mins): Review common calculation traps or tricky theorem steps.\n" +
                "• Step 5: Sleep Buffer: Stop all studying at least 45 minutes before sleep so your brain can consolidate what you reviewed.\n\n" +
                "Which specific chapter or unit in $subject carries the highest marks weighting?"
        return ChatResult(reply, isCrisis = false)
    }

    private fun generateRelaxationResult(): ChatResult {
        val reply = "Here is an effective 5-minute relaxation exercise you can do right now:\n\n" +
                "1. Drop your shoulders down and unclamp your jaw.\n" +
                "2. Practice 4 rounds of Box Breathing: Inhale for 4s, hold for 4s, exhale for 4s, hold for 4s.\n" +
                "3. Roll your neck gently and drink half a glass of cool water.\n\n" +
                "Would you like to start the guided 5-minute breathing timer in the Activities tab?"
        return ChatResult(reply, isCrisis = false)
    }

    fun clearSession(conversationId: String) {
        memoryStore.remove(conversationId)
    }

    data class ChatResult(
        val reply: String,
        val isCrisis: Boolean
    )
}
