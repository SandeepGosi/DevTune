package android.view;

/**
 * Narrowly scoped AOSP WindowManager interface for animation scale operations.
 */
interface IWindowManager {
    void setAnimationScale(int which, float scale);
}
