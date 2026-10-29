import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../providers/auth_provider.dart';
import '../../providers/journal_provider.dart';

class JournalScreen extends StatefulWidget {
  const JournalScreen({super.key});

  @override
  State<JournalScreen> createState() => _JournalScreenState();
}

class _JournalScreenState extends State<JournalScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final auth = Provider.of<AuthProvider>(context, listen: false);
      Provider.of<JournalProvider>(context, listen: false).loadJournals(auth.userEmail);
    });
  }

  void _showAddJournalDialog() {
    final titleController = TextEditingController();
    final contentController = TextEditingController();
    String mood = '😊';

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('New Journal Entry'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(controller: titleController, decoration: const InputDecoration(labelText: 'Title')),
            const SizedBox(height: 12),
            TextField(controller: contentController, decoration: const InputDecoration(labelText: 'Write your thoughts...'), maxLines: 3),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.of(ctx).pop(), child: const Text('Cancel')),
          ElevatedButton(
            onPressed: () async {
              if (titleController.text.trim().isEmpty) return;
              final auth = Provider.of<AuthProvider>(context, listen: false);
              final journalProv = Provider.of<JournalProvider>(context, listen: false);
              await journalProv.addJournal(
                userId: auth.userEmail,
                title: titleController.text.trim(),
                content: contentController.text.trim(),
                moodEmoji: mood,
              );
              if (ctx.mounted) Navigator.of(ctx).pop();
            },
            child: const Text('Save Entry'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final journalProv = Provider.of<JournalProvider>(context);
    final auth = Provider.of<AuthProvider>(context);

    return Scaffold(
      appBar: AppBar(title: const Text('Personal Journal 📓')),
      floatingActionButton: FloatingActionButton(
        onPressed: _showAddJournalDialog,
        backgroundColor: AppColors.primaryGreen,
        child: const Icon(Icons.add, color: Colors.white),
      ),
      body: journalProv.journals.isEmpty
          ? const Center(child: Text('No journal entries yet. Tap + to write one!'))
          : ListView.builder(
              padding: const EdgeInsets.all(18),
              itemCount: journalProv.journals.length,
              itemBuilder: (context, index) {
                final item = journalProv.journals[index];
                return Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  child: ListTile(
                    title: Text(item.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                    subtitle: Text(item.content, maxLines: 2, overflow: TextOverflow.ellipsis),
                    leading: Text(item.moodEmoji, style: const TextStyle(fontSize: 24)),
                    trailing: IconButton(
                      icon: const Icon(Icons.delete_outline, color: Colors.red),
                      onPressed: () {
                        if (item.id != null) {
                          journalProv.deleteJournal(item.id!, auth.userEmail);
                        }
                      },
                    ),
                  ),
                );
              },
            ),
    );
  }
}
