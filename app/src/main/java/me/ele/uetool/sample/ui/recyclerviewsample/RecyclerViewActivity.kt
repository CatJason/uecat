package me.ele.uetool.sample.ui.recyclerviewsample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.sample.R

class RecyclerViewActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)

        val list = List(1000) { i ->
            when (i % 3) {
                0 -> Item1()
                1 -> Item2()
                else -> Item3()
            }
        }

        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SimpleAdapter(list)
    }
}

class SimpleAdapter(private val items: List<Item>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE1 = 1
        private const val TYPE2 = 2
        private const val TYPE3 = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_simple_textview, parent, false)

        return when (viewType) {
            TYPE2 -> ViewHolder2(view)
            TYPE3 -> ViewHolder3(view)
            else -> ViewHolder1(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val view = holder.itemView as TextView
        view.text = position.toString()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is Item1 -> TYPE1
        is Item2 -> TYPE2
        is Item3 -> TYPE3
        else -> 0
    }
}

interface Item

class Item1 : Item
class Item2 : Item
class Item3 : Item

class ViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView)
class ViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView)
class ViewHolder3(itemView: View) : RecyclerView.ViewHolder(itemView)