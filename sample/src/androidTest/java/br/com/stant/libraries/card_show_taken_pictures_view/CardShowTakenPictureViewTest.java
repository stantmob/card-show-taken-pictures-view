package br.com.stant.libraries.card_show_taken_pictures_view;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by stant on 04/07/17.
 */
/**
 * Ui tests for the implementation of {@link MainActivity}
 */
@RunWith(Enclosed.class)
@LargeTest
public class CardShowTakenPictureViewTest {

//    public abstract static class Describe_card_show_taken_picture_view{
//
//        ActivityTestRule<MainActivity> activity =
//                new ActivityTestRule<MainActivity>(MainActivity.class, true, false);
//
//
//        Gallery gallery;
//
//        String image1       = "http://netarus.com/wp-content/uploads/2015/01/hoistcam_on_excavator1.jpg";
//        String image2       = "https://themoscowtimes.com/static/uploads_new/publications/2016/7/1/0657deed994e4bc398b8ac31da58ba80.jpg";
//        String image3       = "http://blog.lareviewofbooks.org/wp-content/uploads/image_carousel_thumbs/IMG_1272-1-n3pkuv4qp03587r641dper3nvsa5ywo3af06m5xd2o.jpg";
//
//        @Before
//        public void before(){
//            setup();
//            Intent intent = IntentFactory.createIntentWithBundle(MainActivity.class, generateBundle());
//            activity.launchActivity(intent);
//        }
//
//        private Bundle generateBundle()
//        {
//            Bundle bundle = new Bundle();
//            bundle.putSerializable(MainActivity.KEY_GALLERY, gallery);
//            return bundle;
//        }
//
//        protected void setup(){}
//    }
//
//
//    public abstract static class Context_when_set_images_correctly extends Describe_card_show_taken_picture_view {
//
//        @Override
//        public void setup(){
//            List<String> images = new ArrayList<>();
//            images.add(image1);
//            images.add(image2);
//            images.add(image3);
//
//            gallery = new Gallery("id-1",images);
//        }
//
//    }
//
//    public static class Context_when_set_null_images extends Describe_card_show_taken_picture_view {
//
//        @Override
//        public void setup(){
//            gallery = new Gallery("id-1",null);
//        }
//
//        @Test
//        public void It_should_check_if_show_edit_state_of_card(){
//            onView(withId(R.id.card_show_taken_picture_save_text_container)).check(matches(isDisplayed()));
//            onView(withId(R.id.card_show_taken_picture_add_picture_container)).check(matches(isDisplayed()));
//        }
//
//    }
//    public static class Context_when_set_empty_arraylist extends Describe_card_show_taken_picture_view {
//
//        @Override
//        public void setup(){
//            gallery = new Gallery("id-1",new ArrayList<String>());
//        }
//
//        @Test
//        public void It_should_check_if_show_edit_state_of_card(){
//            onView(withId(R.id.card_show_taken_picture_save_text_container)).check(matches(isDisplayed()));
//            onView(withId(R.id.card_show_taken_picture_add_picture_container)).check(matches(isDisplayed()));
//        }
//
//    }
//
//    public static class Context_when_has_images extends Context_when_set_images_correctly {
//
//        @Test
//        public void It_should_show_images_correctly(){
//            onView(withRecyclerView(R.id.card_show_taken_picture_image_list_recycler_view)
//                    .atPositionOnView(0, R.id.card_show_taken_picture_view_general_circular_image_view)).check(matches(isDisplayed()));
//        }
//
//        @Test
//        public void It_should_show_normal_state_of_card(){
//            onView(withId(R.id.card_show_taken_picture_edit_icon_container_linear_layout)).check(matches(isDisplayed()));
//        }
//
//    }
//
//    public abstract static class Context_when_click_in_image extends Context_when_set_images_correctly {
//
//        @Before
//        public void click_in_image(){
//            onView(withRecyclerView(R.id.card_show_taken_picture_image_list_recycler_view)
//                    .atPositionOnView(0, R.id.card_show_taken_picture_view_general_circular_image_view)).perform(click());
//        }
//    }
//
//    public static class Context_when_open_preview_dialog extends Context_when_click_in_image{
//        @Test
//        public void It_should_show_image_in_dialog_correctly(){
//            checkIfIdIsDisplayed(R.id.preview_image);
//        }
//    }
//
//    public static class Context_when_click_in_close_preview_image extends  Context_when_click_in_image {
//
//        @Before
//        public void click_in_close_button(){
//            findAndClick(R.id.close_preview_image);
//        }
//
//        @Test
//        public void It_should_dismiss_dialog_and_show_card_correctly(){
//            checkIfIdIsDisplayed(R.id.card_show_taken_picture_image_list_recycler_view);
//        }
//    }



}
