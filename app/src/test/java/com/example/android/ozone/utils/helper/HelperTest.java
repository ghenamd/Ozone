package com.example.android.ozone.utils.helper;

import com.example.android.ozone.model.JsonData;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HelperTest {

    @Mock
    JsonData mData;
    @Mock
    JsonData mJsonData;



    @Test
    public void getFirstListItem() {
        //If we use Mockito.mock(Arraylist.class) it doesn't add any items to the arraylist
        List list = Mockito.spy(new ArrayList());
        list.add(mData);
        Mockito.verify(list).add(mData);

        Helper helper = new Helper();
        assertNotNull(helper.getFirstListItem(list));
    }

    @Test
    public void checkIfNull(){
        List list = Mockito.spy(new ArrayList());
        list.add(mData);
        list.add(mJsonData);
        Helper helper = new Helper();

        when(helper.getFirstListItem(list)).thenReturn(mData);

        assertEquals(helper.getFirstListItem(list),mData);
    }
}