package com.example.rise_of_city.ui.game.building;

import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment cho màn hình kéo thả vật phẩm vào phòng
 * Người dùng kéo các vật phẩm từ panel dưới vào phòng
 */
public class FurnitureDragFragment extends Fragment {

    private FrameLayout roomContainer;
    private LinearLayout furnitureItemsContainer;
    private Button btnNext;
    private TextView tvTitle;

    // Room type: "bedroom" or "livingroom"
    private String roomType = "bedroom";

    // Furniture items data
    private List<FurnitureItem> furnitureItems;
    private Map<String, PlacedFurniture> placedFurniture = new HashMap<>();
    
    // Correct placement positions (x, y relative to room container)
    private Map<String, PlacementZone> correctPlacements = new HashMap<>();
    
    // Placeholder zones to show where furniture can be placed
    private Map<String, View> placeholderZones = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_furniture_drag, container, false);

        roomContainer = view.findViewById(R.id.room_container);
        furnitureItemsContainer = view.findViewById(R.id.furniture_items_container);
        btnNext = view.findViewById(R.id.btn_next);
        tvTitle = view.findViewById(R.id.tv_title);

        // Get room type from arguments
        if (getArguments() != null) {
            roomType = getArguments().getString("room_type", "bedroom");
        }

        setupRoom();
        setupFurnitureItems();
        setupCorrectPlacements();
        
        // Wait for room container to be laid out before creating placeholders
        roomContainer.post(() -> {
            createPlaceholderZones();
        });

        btnNext.setOnClickListener(v -> {
            // Show full room view
            showFullRoom();
        });

        return view;
    }

    private void setupRoom() {
        // Set background based on room type
        if ("bedroom".equals(roomType)) {
            roomContainer.setBackgroundResource(R.drawable.bedroom_bg);
            tvTitle.setText("Bedroom Setup");
        } else if ("kitchen".equals(roomType)) {
            roomContainer.setBackgroundResource(getDrawableRes("kitchen"));
            tvTitle.setText("Kitchen Setup");
        } else {
            // Default to livingroom
            roomContainer.setBackgroundResource(R.drawable.livingroom_bg);
            tvTitle.setText("Living Room Setup");
        }
    }

    private void setupFurnitureItems() {
        furnitureItems = new ArrayList<>();
        
        if ("bedroom".equals(roomType)) {
            // Bedroom furniture - 4 items: bed, sofa, armchair, desk
            furnitureItems.add(new FurnitureItem("bed", getDrawableRes("bedroom_object"), 0.2f, 0.6f));
            furnitureItems.add(new FurnitureItem("sofa", getDrawableRes("sofa1"), 0.4f, 0.5f));
            furnitureItems.add(new FurnitureItem("armchair", getDrawableRes("chair"), 0.3f, 0.4f));
            furnitureItems.add(new FurnitureItem("desk", getDrawableRes("table"), 0.1f, 0.3f));
        } else if ("kitchen".equals(roomType)) {
            // Kitchen furniture - only items from Kitchen Object folder
            furnitureItems.add(new FurnitureItem("fridge", getDrawableRes("fridge"), 0.2f, 0.5f));
            furnitureItems.add(new FurnitureItem("kitchen_cabinets", getDrawableRes("kitchen_cabinets"), 0.5f, 0.4f));
            furnitureItems.add(new FurnitureItem("kitchen_wall_cabinet", getDrawableRes("kitchen_wall_cabinet"), 0.7f, 0.2f));
        } else {
            // Living room furniture - only items from Livingroom Object folder
            furnitureItems.add(new FurnitureItem("sofa", getDrawableRes("sofa1"), 0.5f, 0.5f));
            furnitureItems.add(new FurnitureItem("table", getDrawableRes("table"), 0.3f, 0.4f));
            furnitureItems.add(new FurnitureItem("chair", getDrawableRes("chair"), 0.2f, 0.5f));
            furnitureItems.add(new FurnitureItem("bookshelf", getDrawableRes("bookshelf"), 0.7f, 0.3f));
            furnitureItems.add(new FurnitureItem("heater", getDrawableRes("heater"), 0.8f, 0.6f));
        }

        // Add furniture items to panel
        for (FurnitureItem item : furnitureItems) {
            ImageView furnitureView = createFurnitureView(item);
            furnitureItemsContainer.addView(furnitureView);
        }
    }
    
    private int getDrawableRes(String name) {
        // Get resource ID by name (handling spaces in filename)
        // Android resource names cannot have spaces, so we need to handle them
        String resourceName = name.toLowerCase().replace(" ", "_");
        int resId = getResources().getIdentifier(resourceName, "drawable", getContext().getPackageName());
        if (resId == 0) {
            // Try with original name if lowercase doesn't work
            resId = getResources().getIdentifier(name, "drawable", getContext().getPackageName());
        }
        // Fallback to a default image if still not found
        if (resId == 0) {
            resId = R.drawable.bg;
        }
        return resId;
    }

    private ImageView createFurnitureView(FurnitureItem item) {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(item.drawableRes);
        imageView.setTag(item.name);
        
        // Set size
        int size = (int) (getResources().getDisplayMetrics().density * 80);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(8, 8, 8, 8);
        imageView.setLayoutParams(params);
        
        // Enable drag
        imageView.setOnTouchListener(new FurnitureDragListener());
        
        return imageView;
    }

    private void setupCorrectPlacements() {
        // Define correct placement zones for each furniture item
        if ("bedroom".equals(roomType)) {
            // 3 placeholder zones on the floor: near window, middle, front
            // Zone 1: Near window (for bed) - bottom left area
            correctPlacements.put("bed", new PlacementZone(0.1f, 0.55f, 0.3f, 0.75f));
            // Zone 2: Middle of room (for sofa or armchair) - center area
            correctPlacements.put("sofa", new PlacementZone(0.35f, 0.45f, 0.55f, 0.65f));
            correctPlacements.put("armchair", new PlacementZone(0.35f, 0.45f, 0.55f, 0.65f));
            // Zone 3: Front area (for desk) - bottom right area
            correctPlacements.put("desk", new PlacementZone(0.6f, 0.25f, 0.8f, 0.45f));
        } else if ("kitchen".equals(roomType)) {
            // Kitchen placement zones
            correctPlacements.put("fridge", new PlacementZone(0.1f, 0.45f, 0.3f, 0.65f));
            correctPlacements.put("kitchen_cabinets", new PlacementZone(0.45f, 0.35f, 0.65f, 0.55f));
            correctPlacements.put("kitchen_wall_cabinet", new PlacementZone(0.65f, 0.15f, 0.85f, 0.35f));
        } else {
            // Living room placement zones
            correctPlacements.put("sofa", new PlacementZone(0.4f, 0.45f, 0.6f, 0.65f));
            correctPlacements.put("table", new PlacementZone(0.25f, 0.35f, 0.45f, 0.55f));
            correctPlacements.put("chair", new PlacementZone(0.15f, 0.45f, 0.35f, 0.65f));
            correctPlacements.put("bookshelf", new PlacementZone(0.65f, 0.25f, 0.85f, 0.45f));
            correctPlacements.put("heater", new PlacementZone(0.75f, 0.55f, 0.95f, 0.75f));
        }
    }
    
    private void createPlaceholderZones() {
        // Create placeholder zones for each furniture item
        for (FurnitureItem item : furnitureItems) {
            PlacementZone zone = correctPlacements.get(item.name);
            if (zone != null) {
                View placeholder = createPlaceholderView(zone, item.name);
                placeholderZones.put(item.name, placeholder);
                roomContainer.addView(placeholder);
            }
        }
    }
    
    private View createPlaceholderView(PlacementZone zone, String furnitureName) {
        View placeholder = new View(getContext());
        placeholder.setBackgroundResource(R.drawable.bg_placeholder_zone);
        placeholder.setTag("placeholder_" + furnitureName);
        placeholder.setAlpha(0.7f);
        
        // Calculate size and position based on room container dimensions
        int containerWidth = roomContainer.getWidth();
        int containerHeight = roomContainer.getHeight();
        
        int left = (int) (zone.minX * containerWidth);
        int top = (int) (zone.minY * containerHeight);
        int width = (int) ((zone.maxX - zone.minX) * containerWidth);
        int height = (int) ((zone.maxY - zone.minY) * containerHeight);
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.leftMargin = left;
        params.topMargin = top;
        placeholder.setLayoutParams(params);
        
        return placeholder;
    }

    private class FurnitureDragListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Start drag
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDragAndDrop(null, shadowBuilder, view, 0);
                view.setVisibility(View.INVISIBLE);
                return true;
            }
            return false;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Set up drag and drop listener for room container
        roomContainer.setOnDragListener(new RoomDragListener());
    }

    private class RoomDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                    
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setAlpha(0.8f);
                    // Highlight matching placeholder zone
                    highlightPlaceholderZone((String) ((View) event.getLocalState()).getTag());
                    return true;
                    
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setAlpha(1.0f);
                    // Remove highlight
                    clearPlaceholderHighlights();
                    return true;
                    
                case DragEvent.ACTION_DROP:
                    v.setAlpha(1.0f);
                    View draggedView = (View) event.getLocalState();
                    String furnitureName = (String) draggedView.getTag();
                    
                    // Calculate drop position relative to room container
                    float x = event.getX();
                    float y = event.getY();
                    
                    // Place furniture
                    placeFurniture(furnitureName, x, y, draggedView);
                    
                    // Make original view visible again (for re-dragging)
                    draggedView.setVisibility(View.VISIBLE);
                    return true;
                    
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setAlpha(1.0f);
                    View view = (View) event.getLocalState();
                    if (view != null) {
                        view.setVisibility(View.VISIBLE);
                    }
                    return true;
            }
            return false;
        }
    }

    private void placeFurniture(String furnitureName, float x, float y, View originalView) {
        // Remove existing placement if any
        if (placedFurniture.containsKey(furnitureName)) {
            roomContainer.removeView(placedFurniture.get(furnitureName).view);
        }

        // Create new ImageView for placed furniture
        ImageView placedView = new ImageView(getContext());
        FurnitureItem item = getFurnitureItem(furnitureName);
        if (item != null) {
            placedView.setImageResource(item.drawableRes);
        }
        
        // Set size
        int size = (int) (getResources().getDisplayMetrics().density * 100);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        
        // Center on drop position
        params.leftMargin = (int) (x - size / 2);
        params.topMargin = (int) (y - size / 2);
        
        // Keep within bounds
        if (params.leftMargin < 0) params.leftMargin = 0;
        if (params.topMargin < 0) params.topMargin = 0;
        if (params.leftMargin + size > roomContainer.getWidth()) {
            params.leftMargin = roomContainer.getWidth() - size;
        }
        if (params.topMargin + size > roomContainer.getHeight()) {
            params.topMargin = roomContainer.getHeight() - size;
        }
        
        placedView.setLayoutParams(params);
        
        // Check if placement is correct
        PlacementZone correctZone = correctPlacements.get(furnitureName);
        boolean isCorrect = false;
        if (correctZone != null) {
            float relativeX = params.leftMargin / (float) roomContainer.getWidth();
            float relativeY = params.topMargin / (float) roomContainer.getHeight();
            
            isCorrect = (relativeX >= correctZone.minX && relativeX <= correctZone.maxX &&
                        relativeY >= correctZone.minY && relativeY <= correctZone.maxY);
        }
        
        // Add feedback icon
        ImageView feedbackIcon = new ImageView(getContext());
        feedbackIcon.setImageResource(isCorrect ? R.drawable.correct : R.drawable.incorrect);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
            (int) (getResources().getDisplayMetrics().density * 30),
            (int) (getResources().getDisplayMetrics().density * 30)
        );
        iconParams.leftMargin = params.leftMargin + size / 2;
        iconParams.topMargin = params.topMargin;
        feedbackIcon.setLayoutParams(iconParams);
        
        roomContainer.addView(placedView);
        roomContainer.addView(feedbackIcon);
        
        // Store placement
        placedFurniture.put(furnitureName, new PlacedFurniture(placedView, feedbackIcon, isCorrect));
        
        // Hide placeholder zone if placed correctly
        if (isCorrect) {
            View placeholder = placeholderZones.get(furnitureName);
            if (placeholder != null) {
                placeholder.setVisibility(View.GONE);
            }
            Toast.makeText(getContext(), "Correct placement!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Try a different position", Toast.LENGTH_SHORT).show();
        }
        
        // Clear highlights
        clearPlaceholderHighlights();
        
        checkCompletion();
    }
    
    private void highlightPlaceholderZone(String furnitureName) {
        View placeholder = placeholderZones.get(furnitureName);
        if (placeholder != null) {
            placeholder.setAlpha(1.0f);
            placeholder.setBackgroundResource(R.drawable.bg_placeholder_zone);
        }
    }
    
    private void clearPlaceholderHighlights() {
        for (View placeholder : placeholderZones.values()) {
            if (placeholder.getVisibility() == View.VISIBLE) {
                placeholder.setAlpha(0.7f);
            }
        }
    }

    private FurnitureItem getFurnitureItem(String name) {
        for (FurnitureItem item : furnitureItems) {
            if (item.name.equals(name)) {
                return item;
            }
        }
        return null;
    }

    private void checkCompletion() {
        // Check if all furniture is placed correctly
        boolean allCorrect = true;
        for (PlacedFurniture placed : placedFurniture.values()) {
            if (!placed.isCorrect) {
                allCorrect = false;
                break;
            }
        }
        
        if (allCorrect && placedFurniture.size() == furnitureItems.size()) {
            btnNext.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Perfect! All furniture placed correctly!", Toast.LENGTH_LONG).show();
        }
    }
    
    private void showFullRoom() {
        // Hide furniture panel and show full room image
        View parent = (View) furnitureItemsContainer.getParent();
        if (parent != null) {
            parent.setVisibility(View.GONE);
        }
        btnNext.setVisibility(View.GONE);
        
        // Clear all placed furniture
        roomContainer.removeAllViews();
        
        // Show full room background
        if ("bedroom".equals(roomType)) {
            // bedroom_full.jpg was copied, but Android needs lowercase and no extension in code
            int resId = getResources().getIdentifier("bedroom_full", "drawable", getContext().getPackageName());
            if (resId == 0) {
                resId = R.drawable.bedroom_bg; // Fallback
            }
            roomContainer.setBackgroundResource(resId);
        } else if ("kitchen".equals(roomType)) {
            int resId = getDrawableRes("kitchen_full");
            roomContainer.setBackgroundResource(resId);
        } else {
            int resId = getDrawableRes("livingroom_full");
            roomContainer.setBackgroundResource(resId);
        }
        
        Toast.makeText(getContext(), "Room completed!", Toast.LENGTH_SHORT).show();
    }

    // Data classes
    private static class FurnitureItem {
        String name;
        int drawableRes;
        float defaultX, defaultY;

        FurnitureItem(String name, int drawableRes, float defaultX, float defaultY) {
            this.name = name;
            this.drawableRes = drawableRes;
            this.defaultX = defaultX;
            this.defaultY = defaultY;
        }
    }

    private static class PlacementZone {
        float minX, minY, maxX, maxY;

        PlacementZone(float minX, float minY, float maxX, float maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }

    private static class PlacedFurniture {
        ImageView view;
        ImageView feedbackIcon;
        boolean isCorrect;

        PlacedFurniture(ImageView view, ImageView feedbackIcon, boolean isCorrect) {
            this.view = view;
            this.feedbackIcon = feedbackIcon;
            this.isCorrect = isCorrect;
        }
    }
}

