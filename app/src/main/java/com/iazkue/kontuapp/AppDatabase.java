package com.iazkue.kontuapp;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.migration.Migration;

@Database(entities = {Society.class, Item.class, Account.class, AccountDetail.class, ItemPrice.class}, version = 4)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract SocietyDao societyDao();
    public abstract ItemDao itemDao();
    public abstract AccountDao accountDao();
    public abstract AccountDetailDao accountDetailDao();
    public abstract ItemPriceDao itemPriceDao();

    private static volatile AppDatabase INSTANCE;

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "database-name")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    new Thread(() -> {
                                        AppDatabase database = getDatabase(context);
                                        database.itemDao().insertAll(getInitialItems());
                                    }).start();
                                }
                            })
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Account ADD COLUMN dateCreated INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("UPDATE Account SET dateCreated = strftime('%s', 'now') WHERE dateCreated = 0");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `ItemPrice` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `societyId` INTEGER NOT NULL, `itemId` INTEGER NOT NULL, `price` REAL NOT NULL)");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // No schema changes, just increment the version
            database.execSQL("CREATE TABLE IF NOT EXISTS `Item_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT)");
            database.execSQL("INSERT INTO `Item_new` (id, name) SELECT id, name FROM `Item`");
            database.execSQL("DROP TABLE `Item`");
            database.execSQL("ALTER TABLE `Item_new` RENAME TO `Item`");
        }
    };

    private static Item[] getInitialItems() {
        return new Item[]{
                new Item("zerbitzua"),
                new Item("jangabezerbitzua"),
                new Item("kokakola"),
                new Item("kaslimon"),
                new Item("kasnaranja"),
                new Item("tonica"),
                new Item("bitterkas"),
                new Item("radler"),
                new Item("mahou"),
                new Item("amstel"),
                new Item("estrellagalicia"),
                new Item("sanmiguel"),
                new Item("keler"),
                new Item("alhambra"),
                new Item("bestezerbeza"),
                new Item("kafia"),
                new Item("infusiyua"),
                new Item("kolakaua"),
                new Item("barcelo"),
                new Item("lanavarra"),
                new Item("bacardi"),
                new Item("bombay"),
                new Item("beefeater"),
                new Item("smirnoff"),
                new Item("ginmg"),
                new Item("absolut"),
                new Item("martini"),
                new Item("anisa"),
                new Item("cointreau"),
                new Item("jbwhisky"),
                new Item("gleenfiddich"),
                new Item("torres10"),
                new Item("lepanto"),
                new Item("ruaviejacrema"),
                new Item("orujodehierbas"),
                new Item("armagnac"),
                new Item("baines"),
                new Item("whisky"),
                new Item("aguardientedeorujo"),
                new Item("sagardua"),
                new Item("etab")
        };
    }
}
