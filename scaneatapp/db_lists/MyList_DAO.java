package com.app.project.scaneatapp.db_lists;

import java.util.List;

public interface MyList_DAO {
    void open();
    void close();

    MyList insertList(MyList list);
    void editList(MyList list);
    List<MyList> getAllListsExceptOne(int idList);
    void deleteList(MyList list);
    List<MyList> getAllLists();
}
